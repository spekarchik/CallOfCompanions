package com.pekar.callofcompanions.clientaccess;

import com.pekar.callofcompanions.network.base.ClientToServerPacket;

import java.util.Map;

public interface INetworkClientAccessor
{
    void applyServerConfig(Map<String, String> values);

    void sendToServer(ClientToServerPacket packet);
}
