package com.pekar.callofcompanions.network.base;

import com.pekar.callofcompanions.Main;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.ServerPayloadContext;

public abstract class ClientToServerPacket extends Packet implements IClientToServerPacket
{
    protected ClientToServerPacket()
    {}

    public final void sendToServer()
    {
        var connection = Minecraft.getInstance().getConnection();
        if (connection != null)
        {
            var wrapper = new ServerboundCustomPayloadPacket(this);
            connection.getConnection().send(wrapper);
        }
        else
        {
            Main.LOGGER.warn("Unable to send packet to server: connection is null");
        }
    }

    @Override
    public final boolean isServerToClient()
    {
        return false;
    }

    @Override
    protected final void onReceive(IPayloadContext context)
    {
        var serverContext = (ServerPayloadContext)context;
        onReceive(serverContext.player());
    }
}
