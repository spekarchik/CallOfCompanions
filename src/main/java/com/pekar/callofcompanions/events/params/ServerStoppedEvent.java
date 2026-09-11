package com.pekar.callofcompanions.events.params;

import net.minecraft.server.MinecraftServer;

public final class ServerStoppedEvent
{
    private final MinecraftServer server;

    public ServerStoppedEvent(MinecraftServer server)
    {
        this.server = server;
    }

    public MinecraftServer getServer()
    {
        return server;
    }
}

