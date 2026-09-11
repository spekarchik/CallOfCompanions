package com.pekar.callofcompanions.client;

import com.mojang.logging.LogUtils;
import com.pekar.callofcompanions.ServerConfig;
import java.util.Map;
import com.pekar.callofcompanions.clientaccess.INetworkClientAccessor;
import com.pekar.callofcompanions.network.base.ClientToServerPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import org.slf4j.Logger;

public class NetworkClientAccessor implements INetworkClientAccessor
{
    private static final Logger LOGGER = LogUtils.getLogger();

    private Map<String, String> localSettings;

    @Override
    public void applyServerConfig(Map<String, String> values)
    {
        // The integrated server and its client already share the same config objects.
        if (Minecraft.getInstance().getSingleplayerServer() != null) return;
        var previous = ServerConfig.SPEC.snapshot();
        ServerConfig.SPEC.applySnapshot(values);
        if (localSettings == null) localSettings = previous;
    }

    public void restoreLocalConfig()
    {
        if (localSettings == null) return;
        ServerConfig.SPEC.applySnapshot(localSettings);
        localSettings = null;
    }

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
