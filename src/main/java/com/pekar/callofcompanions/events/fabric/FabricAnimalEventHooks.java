package com.pekar.callofcompanions.events.fabric;

import com.pekar.callofcompanions.events.AnimalEvents;
import com.pekar.callofcompanions.events.params.EntityInteract;
import com.pekar.callofcompanions.events.params.EntityMountEvent;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;

public final class FabricAnimalEventHooks
{
    private static final AnimalEvents ANIMAL_EVENTS = new AnimalEvents();
    private static boolean initialized = false;

    private FabricAnimalEventHooks()
    {}

    public static void init()
    {
        if (initialized) return;
        initialized = true;

        UseEntityCallback.EVENT.register((player, level, hand, entity, hitResult) ->
        {
            ANIMAL_EVENTS.onPlayerInteract(new EntityInteract(player, entity, hand));
            return InteractionResult.PASS;
        });
    }

    public static void onEntityDismount(Entity entityMounting, Entity entityBeingMounted)
    {
        ANIMAL_EVENTS.onEntityMount(new EntityMountEvent(false, entityMounting, entityBeingMounted));
    }
}

