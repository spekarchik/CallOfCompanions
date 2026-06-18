package com.pekar.callofcompanions.client;

import com.mojang.logging.LogUtils;
import com.pekar.callofcompanions.clientaccess.INetworkClientAccessor;
import com.pekar.callofcompanions.network.base.ClientToServerPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import org.slf4j.Logger;

public class NetworkClientAccessor implements INetworkClientAccessor
{
    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void sendToServer(ClientToServerPacket packet)
    {
        var connection = Minecraft.getInstance().getConnection();
        if (connection != null)
        {
            var wrapper = new ServerboundCustomPayloadPacket(packet);
            connection.getConnection().send(wrapper);
        }
        else
        {
            LOGGER.warn("Unable to send packet to server: connection is null");
        }
    }
}
