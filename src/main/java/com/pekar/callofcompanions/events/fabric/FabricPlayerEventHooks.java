package com.pekar.callofcompanions.events.fabric;

import com.pekar.callofcompanions.events.PlayerEvents;
import com.pekar.callofcompanions.events.params.EntityTeleportEvent;
import com.pekar.callofcompanions.events.params.LivingDeathEvent;
import com.pekar.callofcompanions.events.params.LivingEquipmentChangeEvent;
import com.pekar.callofcompanions.events.params.PlayerEvent;
import com.pekar.callofcompanions.events.params.PlayerInteractEvent;
import com.pekar.callofcompanions.items.ItemRegistry;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityLevelChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;

public final class FabricPlayerEventHooks
{
    private static final PlayerEvents PLAYER_EVENTS = new PlayerEvents();
    private static boolean initialized = false;

    private FabricPlayerEventHooks()
    {}

    public static void init()
    {
        if (initialized) return;
        initialized = true;

        UseEntityCallback.EVENT.register((player, level, hand, entity, hitResult) ->
        {
            var held = player.getItemInHand(hand);

            // Client side: suppress vanilla interaction for call crystals, but avoid mutating any state.
            if (level.isClientSide())
            {
                if (entity instanceof net.minecraft.world.entity.player.Player) return InteractionResult.PASS;

                if (!held.is(ItemRegistry.DEEP_CALL_CRYSTAL) && !held.is(ItemRegistry.CALL_CRYSTAL))
                {
                    return InteractionResult.PASS;
                }

                return InteractionResult.CONSUME;
            }

            var event = new PlayerInteractEvent.EntityInteractSpecific(player, level, hand, entity, held, hitResult);
            PLAYER_EVENTS.onPlayerInteractionEvent(event);
            return event.getCancellationResult();
        });

        ServerEntityEvents.EQUIPMENT_CHANGE.register((livingEntity, slot, previousStack, currentStack) ->
        {
            if (livingEntity instanceof ServerPlayer serverPlayer)
            {
                PLAYER_EVENTS.onPlayerEquipmentChangeEvent(new LivingEquipmentChangeEvent(serverPlayer, slot, previousStack, currentStack));
            }
        });

        ServerPlayerEvents.LEAVE.register(player ->
                PLAYER_EVENTS.onPlayerLoggedOutEvent(new PlayerEvent.PlayerLoggedOutEvent(player)));

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) ->
        {
            if (entity instanceof ServerPlayer serverPlayer)
            {
                PLAYER_EVENTS.onPlayerDeathEvent(new LivingDeathEvent(serverPlayer, damageSource));
            }
        });

        ServerEntityLevelChangeEvents.AFTER_PLAYER_CHANGE_LEVEL.register((player, origin, destination) ->
                PLAYER_EVENTS.onPlayerChangedDimensionEvent(new PlayerEvent.PlayerChangedDimensionEvent(player)));

        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) ->
                PLAYER_EVENTS.onPlayerRespawnedEvent(new PlayerEvent.PlayerRespawnEvent(newPlayer)));
    }

    public static void onPlayerTeleport(ServerPlayer player)
    {
        PLAYER_EVENTS.onPlayerTeleportEvent(new EntityTeleportEvent(player));
    }
}

