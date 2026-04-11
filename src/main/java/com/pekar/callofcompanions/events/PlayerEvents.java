package com.pekar.callofcompanions.events;

import com.mojang.logging.LogUtils;
import com.pekar.callofcompanions.data.CompanionData;
import com.pekar.callofcompanions.data.CompanionEntry;
import com.pekar.callofcompanions.data.DataRegistry;
import com.pekar.callofcompanions.data.PositionStatus;
import com.pekar.callofcompanions.items.ItemRegistry;
import com.pekar.callofcompanions.scheduler.CompanionEntryScheduler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
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

        boolean isCallCrystal = itemStack.is(ItemRegistry.CALL_CRYSTAL);
        if (isCallCrystal || itemStack.is(ItemRegistry.DEEP_CALL_CRYSTAL))
        {
            boolean isTameAnimal = target instanceof TamableAnimal tamable && tamable.isTame();
            boolean isTamedHorse = target instanceof AbstractHorse horse && horse.isTamed();

            if (target instanceof Animal animal && (isTameAnimal || isTamedHorse))
            {
                if (event.getLevel() instanceof ServerLevel serverLevel)
                {
                    playAddAnimalSound(serverLevel, animal);
                }

                short dataCapacity = isCallCrystal ? DataRegistry.CRYSTAL_DATA_CAPACITY : DataRegistry.DEEP_CRYSTAL_DATA_CAPACITY;
                var companionData = itemStack.getOrDefault(DataRegistry.COMPANIONS, new CompanionData(dataCapacity));
                var id = itemStack.get(DataRegistry.CRYSTAL_ID);
                if (id == null)
                    itemStack.set(DataRegistry.CRYSTAL_ID, UUID.randomUUID());

                var name = target.getDisplayName().getString();
                var companionType = target.getType().getDescription().getString();
                var entry = new CompanionEntry(
                        target.getUUID(),
                        name,
                        companionType,
                        target.level().dimension(),
                        target.blockPosition(),
                        PositionStatus.FRESH,
                        player.getUUID(),
                        player.getDisplayName().getString());

                companionData.add(entry);
                itemStack.remove(DataRegistry.COMPANIONS);
                itemStack.set(DataRegistry.COMPANIONS, companionData.copy());

                event.setCanceled(true);
                event.setCancellationResult(event.getSide() == LogicalSide.CLIENT ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER);
            }
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
            if (event.getSlot().getType() == EquipmentSlot.Type.HAND)
            {
                var fromUuid = event.getFrom().get(DataRegistry.CRYSTAL_ID);
                var toUuid = event.getTo().get(DataRegistry.CRYSTAL_ID);

                if ((fromUuid == null && toUuid == null) || (fromUuid != null && fromUuid.equals(toUuid))) return;

                LOGGER.debug("Player equipment changed: clear scheduled companion tasks, player={}", serverPlayer.getName().getString());
                CompanionEntryScheduler.DELAY_TASKS.clearFor(serverPlayer);
                CompanionEntryScheduler.TELEPORT_TASKS.clearFor(serverPlayer);
                CompanionEntryScheduler.UPDATE_POS_TASKS.clearFor(serverPlayer);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedOutEvent(PlayerEvent.PlayerLoggedOutEvent event)
    {
        if (event.getEntity() instanceof ServerPlayer serverPlayer)
        {
            CompanionEntryScheduler.DELAY_TASKS.clearFor(serverPlayer);
            CompanionEntryScheduler.TELEPORT_TASKS.clearFor(serverPlayer);
            CompanionEntryScheduler.UPDATE_POS_TASKS.clearFor(serverPlayer);
        }
    }

    @SubscribeEvent
    public void onPlayerDeathEvent(LivingDeathEvent event)
    {
        if (event.getEntity() instanceof ServerPlayer serverPlayer)
        {
            CompanionEntryScheduler.DELAY_TASKS.clearFor(serverPlayer);
            CompanionEntryScheduler.TELEPORT_TASKS.clearFor(serverPlayer);
            CompanionEntryScheduler.UPDATE_POS_TASKS.clearFor(serverPlayer);
        }
    }

    @SubscribeEvent
    public void onPlayerChangedDimensionEvent(PlayerEvent.PlayerChangedDimensionEvent event)
    {
        if (event.getEntity() instanceof ServerPlayer serverPlayer)
        {
            CompanionEntryScheduler.DELAY_TASKS.clearFor(serverPlayer);
            CompanionEntryScheduler.TELEPORT_TASKS.clearFor(serverPlayer);
            CompanionEntryScheduler.UPDATE_POS_TASKS.clearFor(serverPlayer);
        }
    }

    @SubscribeEvent
    public void onPlayerRespawnedEvent(PlayerEvent.PlayerRespawnEvent event)
    {
        if (event.getEntity() instanceof ServerPlayer serverPlayer)
        {
            CompanionEntryScheduler.DELAY_TASKS.clearFor(serverPlayer);
            CompanionEntryScheduler.TELEPORT_TASKS.clearFor(serverPlayer);
            CompanionEntryScheduler.UPDATE_POS_TASKS.clearFor(serverPlayer);
        }
    }

    @SubscribeEvent
    public void onPlayerTeleportEvent(EntityTeleportEvent event)
    {
        if (event.getEntity() instanceof ServerPlayer serverPlayer)
        {
            CompanionEntryScheduler.DELAY_TASKS.clearFor(serverPlayer);
            CompanionEntryScheduler.TELEPORT_TASKS.clearFor(serverPlayer);
            CompanionEntryScheduler.UPDATE_POS_TASKS.clearFor(serverPlayer);
        }
    }
}
