package com.pekar.callofcompanions.events;

import com.mojang.logging.LogUtils;
import com.pekar.callofcompanions.Config;
import com.pekar.callofcompanions.controllers.CallCrystalHelper;
import com.pekar.callofcompanions.data.CompanionData;
import com.pekar.callofcompanions.data.DataRegistry;
import com.pekar.callofcompanions.events.params.EntityTeleportEvent;
import com.pekar.callofcompanions.events.params.LivingDeathEvent;
import com.pekar.callofcompanions.events.params.LivingEquipmentChangeEvent;
import com.pekar.callofcompanions.events.params.PlayerEvent;
import com.pekar.callofcompanions.events.params.PlayerInteractEvent;
import com.pekar.callofcompanions.items.ItemRegistry;
import com.pekar.callofcompanions.scheduler.CompanionEntryScheduler;
import com.pekar.callofcompanions.utils.Players;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;

public class PlayerEvents implements IEventHandler
{
    private static final Logger LOGGER = LogUtils.getLogger();

    public void onPlayerInteractionEvent(PlayerInteractEvent.EntityInteractSpecific event)
    {
        var target = event.getTarget();
        if (target instanceof Player) return;

        var itemStack = event.getItemStack();
        var player = event.getEntity();

        boolean isDeepCallCrystal = itemStack.is(ItemRegistry.DEEP_CALL_CRYSTAL);
        if (!isDeepCallCrystal && !itemStack.is(ItemRegistry.CALL_CRYSTAL)) return;

        if (player.getCooldowns().isOnCooldown(itemStack))
        {
            consumeInteraction(event);
            return;
        }

        if (target instanceof Animal animal)
        {
            handleAnimalCrystalUse(event, player, itemStack, animal, isDeepCallCrystal);
            return;
        }

        if (target instanceof LivingEntity && player instanceof ServerPlayer serverPlayer)
        {
            Players.sendOverlayMessage(serverPlayer, Component.translatable("message.callofcompanions.cant_bind_entity_type", target.getDisplayName()));
        }

        // Keep consume fallback so crystals suppress vanilla entity interactions on failed/non-bind attempts.
        consumeInteraction(event);
    }

    private void handleAnimalCrystalUse(PlayerInteractEvent.EntityInteractSpecific event, Player player, ItemStack itemStack, Animal animal, boolean isDeepCallCrystal)
    {
        // Clean crystals may be stackable, but binding must use exactly one item.
        if (itemStack.getCount() != 1)
        {
            if (player instanceof ServerPlayer serverPlayer)
            {
                Players.sendOverlayMessage(serverPlayer, Component.translatable("message.callofcompanions.single_crystal_only"));
            }

            consumeInteraction(event);
            return;
        }

        boolean allowNamedUntamed = isDeepCallCrystal && Config.DEEP_CRYSTAL_ALLOW_UNTAMED.isTrue();
        if (!CallCrystalHelper.canBindAnimal(animal, allowNamedUntamed))
        {
            if (player instanceof ServerPlayer serverPlayer)
            {
                Players.sendOverlayMessage(serverPlayer, Component.translatable(
                        allowNamedUntamed
                                ? "message.callofcompanions.cant_bind_tame_or_named"
                                : "message.callofcompanions.cant_bind_tame_only"
                ));
            }

            consumeInteraction(event);
            return;
        }

        short dataCapacity = CallCrystalHelper.crystalDataCapacity(isDeepCallCrystal);
        var companionData = itemStack.getOrDefault(DataRegistry.COMPANIONS, new CompanionData(dataCapacity));
        CallCrystalHelper.ensureCrystalId(itemStack);

        var level = event.getLevel();
        var entry = CallCrystalHelper.createCompanionEntry(animal, level.getGameTime());
        if (companionData.add(entry))
        {
            if (level instanceof ServerLevel serverLevel)
            {
                playAddAnimalSound(serverLevel, animal);
            }

            itemStack.remove(DataRegistry.COMPANIONS);
            itemStack.set(DataRegistry.COMPANIONS, companionData.copy());
            succeedInteraction(event);
            return;
        }

        if (player instanceof ServerPlayer serverPlayer)
        {
            Players.sendOverlayMessage(serverPlayer, Component.translatable("message.callofcompanions.limit_reached"));
        }
        consumeInteraction(event);
    }

    private static void consumeInteraction(PlayerInteractEvent.EntityInteractSpecific event)
    {
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.CONSUME);
    }

    private static void succeedInteraction(PlayerInteractEvent.EntityInteractSpecific event)
    {
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }

    public void onPlayerEquipmentChangeEvent(LivingEquipmentChangeEvent event)
    {
        if (event.getEntity() instanceof ServerPlayer serverPlayer)
        {
            var fromItem = event.getFrom();
            var toItem = event.getTo();

            if (event.getSlot().getType() == EquipmentSlot.Type.HAND)
            {
                if (toItem.is(ItemRegistry.CALL_CRYSTALS_TAG) && toItem.getOrDefault(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, false))
                {
                    var fromUuid = fromItem.get(DataRegistry.CRYSTAL_ID);
                    var toUuid = toItem.get(DataRegistry.CRYSTAL_ID);

                    if (!fromItem.is(ItemRegistry.CALL_CRYSTALS_TAG) || fromUuid == null || !fromUuid.equals(toUuid))
                    {
                        toItem.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, false);
                        return;
                    }
                }
            }

            if (event.getSlot() != EquipmentSlot.MAINHAND) return;

            if (!fromItem.is(ItemRegistry.CALL_CRYSTALS_TAG)) return;

            var fromUuid = fromItem.get(DataRegistry.CRYSTAL_ID);
            var toUuid = toItem.get(DataRegistry.CRYSTAL_ID);

            if (fromUuid == null || fromUuid.equals(toUuid)) return;

            LOGGER.debug("Player equipment changed: clear scheduled companion tasks, player={}, fromId={}, toId={}", serverPlayer.getName().getString(), fromUuid, toUuid);
            cancelTasksFor(serverPlayer);
        }
    }

    public void onPlayerLoggedOutEvent(PlayerEvent.PlayerLoggedOutEvent event)
    {
        if (event.getEntity() instanceof ServerPlayer serverPlayer)
        {
            cancelTasksFor(serverPlayer);
        }
    }

    public void onPlayerDeathEvent(LivingDeathEvent event)
    {
        if (event.getEntity() instanceof ServerPlayer serverPlayer)
        {
            cancelTasksFor(serverPlayer);
        }
    }

    public void onPlayerChangedDimensionEvent(PlayerEvent.PlayerChangedDimensionEvent event)
    {
        if (event.getEntity() instanceof ServerPlayer serverPlayer)
        {
            cancelTasksFor(serverPlayer);
        }
    }

    public void onPlayerRespawnedEvent(PlayerEvent.PlayerRespawnEvent event)
    {
        if (event.getEntity() instanceof ServerPlayer serverPlayer)
        {
            cancelTasksFor(serverPlayer);
        }
    }

    public void onPlayerTeleportEvent(EntityTeleportEvent event)
    {
        if (event.getEntity() instanceof ServerPlayer serverPlayer)
        {
            cancelTasksFor(serverPlayer);
        }
    }

    private void playAddAnimalSound(ServerLevel level, Animal animal)
    {
        level.playSound(null, animal.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.1F, 1.6F);
    }

    private static void cancelTasksFor(ServerPlayer player)
    {
        if (CompanionEntryScheduler.hasTasks(player))
            Players.sendOverlayMessage(player, Component.translatable("message.callofcompanions.summon_cancelled"));

        CompanionEntryScheduler.DELAY_TASKS.clearFor(player);
        CompanionEntryScheduler.TELEPORT_TASKS.clearFor(player);
        CompanionEntryScheduler.UPDATE_POS_TASKS.clearFor(player);
    }
}
