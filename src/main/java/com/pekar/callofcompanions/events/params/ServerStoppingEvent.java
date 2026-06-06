package com.pekar.callofcompanions.events.params;

import net.minecraft.server.MinecraftServer;

public final class ServerStoppingEvent
{
    private final MinecraftServer server;

    public ServerStoppingEvent(MinecraftServer server)
    {
        this.server = server;
    }

    public MinecraftServer getServer()
    {
        return server;
    }
}

