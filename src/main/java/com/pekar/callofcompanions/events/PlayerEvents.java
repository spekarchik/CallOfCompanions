package com.pekar.callofcompanions.events;

import com.pekar.callofcompanions.data.CompanionData;
import com.pekar.callofcompanions.data.CompanionEntry;
import com.pekar.callofcompanions.data.DataRegistry;
import com.pekar.callofcompanions.data.PositionStatus;
import com.pekar.callofcompanions.items.ItemRegistry;
import com.pekar.callofcompanions.scheduler.CompanionEntryScheduler;
import net.minecraft.server.level.ServerPlayer;
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

public class PlayerEvents implements IEventHandler
{
    @SubscribeEvent
    public void onPlayerInteractionEvent(PlayerInteractEvent.EntityInteractSpecific event)
    {
        var target = event.getTarget();
        if (target instanceof Player) return;

        var itemStack = event.getItemStack();
        var player = event.getEntity();

        if (itemStack.is(ItemRegistry.CALL_CRYSTAL))
        {
            boolean isTameAnimal = target instanceof TamableAnimal tamable && tamable.isTame();
            boolean isTamedHorse = target instanceof AbstractHorse horse && horse.isTamed();

            if (target instanceof Animal animal && (isTameAnimal || isTamedHorse))
            {
                var companions = itemStack.getOrDefault(DataRegistry.COMPANIONS, new CompanionData());

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

                companions.add(entry);
                itemStack.remove(DataRegistry.COMPANIONS);
                itemStack.set(DataRegistry.COMPANIONS, new CompanionData(companions.getCompanions()));

                event.setCanceled(true);
                event.setCancellationResult(event.getSide() == LogicalSide.CLIENT ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerEquipmentChangeEvent(LivingEquipmentChangeEvent event)
    {
        if (event.getEntity() instanceof ServerPlayer serverPlayer)
        {
            if (event.getSlot().getType() == EquipmentSlot.Type.HAND)
            {
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
