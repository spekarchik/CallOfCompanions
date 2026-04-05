package com.pekar.callofcompanions.events;

import net.neoforged.neoforge.common.NeoForge;

public class EventRegistry
{
    public static void registerEvents()
    {
        register(new PlayerInteractionEvents());
    }

    private static void register(IEventHandler eventHandler)
    {
        NeoForge.EVENT_BUS.register(eventHandler);
    }
}
