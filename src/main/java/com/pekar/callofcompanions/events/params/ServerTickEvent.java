package com.pekar.callofcompanions.events.params;

import net.minecraft.server.MinecraftServer;

public final class ServerTickEvent
{
    private ServerTickEvent()
    {}

    public static final class Post
    {
        private final MinecraftServer server;

        public Post(MinecraftServer server)
        {
            this.server = server;
        }

        public MinecraftServer getServer()
        {
            return server;
        }
    }
}

