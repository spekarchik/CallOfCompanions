package com.pekar.callofcompanions.events;

import com.pekar.callofcompanions.network.NetworkingEventHandler;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;

public class EventRegistry
{
    public static void registerEvents()
    {
        register(new PlayerEvents());
        register(new TickEvents());
        register(new WorldEvents());
    }

    public static void registerEventsOnModBus(IEventBus modEventBus)
    {
        register(modEventBus, new NetworkingEventHandler());
    }

    private static void register(IEventHandler eventHandler)
    {
        NeoForge.EVENT_BUS.register(eventHandler);
    }

    private static void register(IEventBus modEventBus, IEventHandler eventHandler)
    {
        modEventBus.register(eventHandler);
    }
}
