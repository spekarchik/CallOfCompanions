package com.pekar.callofcompanions.events;

import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.world.InteractionResult;

public final class FabricPlayerEventHooks
{
    private static boolean initialized = false;

    private FabricPlayerEventHooks()
    {}

    public static void init()
    {
        if (initialized) return;
        initialized = true;

        UseEntityCallback.EVENT.register((player, level, hand, entity, hitResult) ->
        {
            var event = EntityInteractSpecific.fromFabric(player, level, hand, entity);
            PlayerInteractionHandler.onPlayerInteractionEvent(event);
            return event.isCanceled() ? event.getCancellationResult() : InteractionResult.PASS;
        });
    }
}
