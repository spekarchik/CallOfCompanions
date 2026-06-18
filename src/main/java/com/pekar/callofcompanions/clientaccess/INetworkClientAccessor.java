package com.pekar.callofcompanions.clientaccess;

import com.pekar.callofcompanions.network.base.ClientToServerPacket;

public interface INetworkClientAccessor
{
    void sendToServer(ClientToServerPacket packet);
}
