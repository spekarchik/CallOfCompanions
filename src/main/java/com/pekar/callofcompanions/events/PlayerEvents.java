package com.pekar.callofcompanions.events;

import com.mojang.logging.LogUtils;
import com.pekar.callofcompanions.Config;
import com.pekar.callofcompanions.controllers.CallCrystalHelper;
import com.pekar.callofcompanions.data.CompanionData;
import com.pekar.callofcompanions.data.CompanionEntry;
import com.pekar.callofcompanions.data.DataRegistry;
import com.pekar.callofcompanions.data.PositionStatus;
import com.pekar.callofcompanions.items.ItemRegistry;
import com.pekar.callofcompanions.scheduler.CompanionEntryScheduler;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.slf4j.Logger;

import java.util.Optional;
import java.util.UUID;

public class PlayerEvents implements IEventHandler
{
    private static final Logger LOGGER = LogUtils.getLogger();

    @SubscribeEvent
    public void onPlayerInteractionEvent(PlayerInteractEvent.EntityInteractSpecific event)
    {
        var target = event.getTarget();
        if (target instanceof Player) return;

        var itemStack = event.getItemStack();
        var player = event.getEntity();

        boolean isDeepCallCrystal = itemStack.is(ItemRegistry.DEEP_CALL_CRYSTAL);
        if (isDeepCallCrystal || itemStack.is(ItemRegistry.CALL_CRYSTAL))
        {
            if (player.getCooldowns().isOnCooldown(itemStack))
            {
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.CONSUME);
                return;
            }

            boolean isTameAnimal = target instanceof TamableAnimal tamable && tamable.isTame();
            boolean isTamedHorse = target instanceof AbstractHorse horse && horse.isTamed();

            if (target instanceof Animal animal)
            {
                if (isTameAnimal || isTamedHorse || (isDeepCallCrystal && Config.DEEP_CRYSTAL_DISALLOW_UNTAMED.isFalse() && animal.hasCustomName()))
                {
                    short dataCapacity = isDeepCallCrystal ? (short) Config.DEEP_CRYSTAL_DATA_CAPACITY.getAsInt() : (short) Config.CRYSTAL_DATA_CAPACITY.getAsInt();
                    var companionData = itemStack.getOrDefault(DataRegistry.COMPANIONS, new CompanionData(dataCapacity));
                    var id = itemStack.get(DataRegistry.CRYSTAL_ID);
                    if (id == null)
                        itemStack.set(DataRegistry.CRYSTAL_ID, UUID.randomUUID());

                    var name = target.getDisplayName().getString();
                    var companionType = CallCrystalHelper.getAnimalType(animal);
                    var owner = target instanceof OwnableEntity ownable ? ownable.getOwner() : null;
                    Optional<UUID> ownerId = owner != null ? Optional.of(owner.getUUID()) : Optional.empty();
                    Optional<String> ownerName = owner != null ? Optional.of(owner.getDisplayName().getString()) : Optional.empty();

                    var entry = new CompanionEntry(
                            target.getUUID(),
                            name,
                            companionType,
                            target.level().dimension(),
                            target.blockPosition(),
                            PositionStatus.FRESH,
                            ownerId,
                            ownerName,
                            System.currentTimeMillis());

                    var result = companionData.add(entry);
                    if (result)
                    {
                        if (event.getLevel() instanceof ServerLevel serverLevel)
                        {
                            playAddAnimalSound(serverLevel, animal);
                        }

                        itemStack.remove(DataRegistry.COMPANIONS);
                        itemStack.set(DataRegistry.COMPANIONS, companionData.copy());

                        event.setCanceled(true);
                        event.setCancellationResult(event.getSide() == LogicalSide.CLIENT ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER);
                        return;
                    }
                    else if (player instanceof ServerPlayer serverPlayer)
                    {
                        serverPlayer.sendOverlayMessage(Component.translatable("message.callofcompanions.limit_reached"));
                    }
                }
            }

            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.CONSUME);
        }
    }

    private void playAddAnimalSound(ServerLevel level, Animal animal)
    {
        level.playSound(null, animal.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.1F, 1.6F);
    }

    @SubscribeEvent
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

    @SubscribeEvent
    public void onPlayerLoggedOutEvent(PlayerEvent.PlayerLoggedOutEvent event)
    {
        if (event.getEntity() instanceof ServerPlayer serverPlayer)
        {
            cancelTasksFor(serverPlayer);
        }
    }

    @SubscribeEvent
    public void onPlayerDeathEvent(LivingDeathEvent event)
    {
        if (event.getEntity() instanceof ServerPlayer serverPlayer)
        {
            cancelTasksFor(serverPlayer);
        }
    }

    @SubscribeEvent
    public void onPlayerChangedDimensionEvent(PlayerEvent.PlayerChangedDimensionEvent event)
    {
        if (event.getEntity() instanceof ServerPlayer serverPlayer)
        {
            cancelTasksFor(serverPlayer);
        }
    }

    @SubscribeEvent
    public void onPlayerRespawnedEvent(PlayerEvent.PlayerRespawnEvent event)
    {
        if (event.getEntity() instanceof ServerPlayer serverPlayer)
        {
            cancelTasksFor(serverPlayer);
        }
    }

    @SubscribeEvent
    public void onPlayerTeleportEvent(EntityTeleportEvent event)
    {
        if (event.getEntity() instanceof ServerPlayer serverPlayer)
        {
            cancelTasksFor(serverPlayer);
        }
    }

    private static void cancelTasksFor(ServerPlayer player)
    {
        if (CompanionEntryScheduler.hasTasks(player))
            player.sendOverlayMessage(Component.translatable("message.callofcompanions.summon_cancelled"));

        CompanionEntryScheduler.DELAY_TASKS.clearFor(player);
        CompanionEntryScheduler.TELEPORT_TASKS.clearFor(player);
        CompanionEntryScheduler.UPDATE_POS_TASKS.clearFor(player);
    }
}
