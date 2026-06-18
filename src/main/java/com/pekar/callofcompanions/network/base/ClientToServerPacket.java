package com.pekar.callofcompanions.network.base;

import com.pekar.callofcompanions.clientaccess.ClientAccessor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public abstract class ClientToServerPacket extends Packet implements IClientToServerPacket
{
    protected ClientToServerPacket()
    {}

    public final void sendToServer()
    {
        ClientAccessor.networkClientAccessor().sendToServer(this);
    }

    @Override
    public final boolean isServerToClient()
    {
        return false;
    }

    @Override
    protected final void onReceive(Player player)
    {
        onReceive((ServerPlayer) player);
    }
}