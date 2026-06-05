package com.pekar.callofcompanions.events;

import com.mojang.logging.LogUtils;
import com.pekar.callofcompanions.scheduler.CompanionEntryScheduler;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
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
    public void onPlayerInteractionEventNeoForge(PlayerInteractEvent.EntityInteractSpecific event)
    {
        var side = event.getSide() == net.neoforged.fml.LogicalSide.CLIENT ? LogicalSide.CLIENT : LogicalSide.SERVER;
        var wrapper = new EntityInteractSpecific(event.getEntity(), event.getLevel(), event.getTarget(), event.getItemStack(), side);

        PlayerInteractionHandler.onPlayerInteractionEvent(wrapper);

        if (wrapper.isCanceled())
        {
            event.setCanceled(true);
            event.setCancellationResult(wrapper.getCancellationResult());
        }
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
