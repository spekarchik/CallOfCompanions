package com.pekar.callofcompanions.events.fabric;

import com.pekar.callofcompanions.events.TickEvents;
import com.pekar.callofcompanions.events.WorldEvents;
import com.pekar.callofcompanions.events.params.ServerStoppingEvent;
import com.pekar.callofcompanions.events.params.ServerTickEvent;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

public final class FabricLifecycleEventHooks
{
    private static final TickEvents TICK_EVENTS = new TickEvents();
    private static final WorldEvents WORLD_EVENTS = new WorldEvents();
    private static boolean initialized = false;

    private FabricLifecycleEventHooks()
    {}

    public static void init()
    {
        if (initialized) return;
        initialized = true;

        ServerTickEvents.END_SERVER_TICK.register(server ->
                TICK_EVENTS.onServerTickEvent(new ServerTickEvent.Post(server)));

        ServerLifecycleEvents.SERVER_STOPPING.register(server ->
                WORLD_EVENTS.onServerStoppingEvent(new ServerStoppingEvent(server)));
    }
}

