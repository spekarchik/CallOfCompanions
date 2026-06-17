package com.pekar.callofcompanions.network.base;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.chunk.LevelChunk;

public abstract class ServerToClientPacket extends Packet implements IServerToClientPacket
{
    protected ServerToClientPacket()
    {}

    public final void sendToPlayer(ServerPlayer player)
    {
        ServerPlayNetworking.send(player, this);
    }

    public final void sendToEntity(Entity entity)
    {
        for (var player : PlayerLookup.tracking(entity))
        {
            ServerPlayNetworking.send(player, this);
        }
    }

    public final void sendToChunk(LevelChunk chunk)
    {
        var level = (ServerLevel) chunk.getLevel();
        var chunkPos = chunk.getPos();

        for (var player : PlayerLookup.tracking(level, chunkPos))
        {
            ServerPlayNetworking.send(player, this);
        }
    }

    @Override
    public final boolean isServerToClient()
    {
        return true;
    }
}
