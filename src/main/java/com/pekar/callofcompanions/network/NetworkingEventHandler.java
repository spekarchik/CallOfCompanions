package com.pekar.callofcompanions.network;

import com.pekar.callofcompanions.events.IEventHandler;

public final class NetworkingEventHandler implements IEventHandler
{
    public NetworkingEventHandler()
    {
        Networking.init();
    }
}
