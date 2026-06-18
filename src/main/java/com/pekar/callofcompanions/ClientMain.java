package com.pekar.callofcompanions;

import com.pekar.callofcompanions.client.ItemsClientAccessor;
import com.pekar.callofcompanions.client.NetworkClientAccessor;
import com.pekar.callofcompanions.clientaccess.ClientAccessor;
import com.pekar.callofcompanions.network.Networking;
import net.fabricmc.api.ClientModInitializer;

public class ClientMain implements ClientModInitializer
{
    @Override
    public void onInitializeClient()
    {
        Networking.initClient();
        ClientAccessor.init(
                new NetworkClientAccessor(),
                new ItemsClientAccessor()
        );
    }
}
