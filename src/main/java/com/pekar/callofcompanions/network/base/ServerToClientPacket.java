package com.pekar.callofcompanions.network.base;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.network.PacketDistributor;

public abstract class ServerToClientPacket extends Packet implements IServerToClientPacket
{
    protected ServerToClientPacket()
    {}

    public final void sendToPlayer(ServerPlayer player)
    {
        PacketDistributor.sendToPlayer(player, this);
    }

    public final void sendToEntity(Entity entity)
    {
        PacketDistributor.sendToPlayersTrackingEntity(entity, this);
    }

    public final void sendToChunk(LevelChunk chunk)
    {
        PacketDistributor.sendToPlayersTrackingChunk((ServerLevel) chunk.getLevel(), chunk.getPos(), this);
    }

    @Override
    public final boolean isServerToClient()
    {
        return true;
    }
}
