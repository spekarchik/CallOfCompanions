package com.pekar.callofcompanions.network.base;

import net.minecraft.server.level.ServerPlayer;

public interface IClientToServerPacket extends IPacket
{
    void sendToServer();
    void onReceive(ServerPlayer serverPlayer);
}
