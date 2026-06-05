package com.pekar.callofcompanions.network.base;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.chunk.LevelChunk;

public interface IServerToClientPacket extends IPacket
{
    void sendToPlayer(ServerPlayer player);
    void sendToEntity(Entity entity);
    void sendToChunk(LevelChunk chunk);
    void onReceive(LocalPlayer player);
}
