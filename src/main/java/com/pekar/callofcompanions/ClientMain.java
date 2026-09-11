package com.pekar.callofcompanions;

import com.pekar.callofcompanions.client.ItemsClientAccessor;
import com.pekar.callofcompanions.client.NetworkClientAccessor;
import com.pekar.callofcompanions.clientaccess.ClientAccessor;
import com.pekar.callofcompanions.data.TrackingPreferences;
import com.pekar.callofcompanions.network.Networking;
import com.pekar.callofcompanions.network.TrackingPreferencesPacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;

public class ClientMain implements ClientModInitializer
{
    @Override
    public void onInitializeClient()
    {
        var configDirectory = FabricLoader.getInstance().getConfigDir();
        ConfigMigration.migrate(configDirectory, "client");
        try
        {
            ClientConfig.SPEC.load(configDirectory.resolve(Main.MODID + "-client.toml"));
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to load client config", e);
        }

        Networking.initClient();
        var networkClientAccessor = new NetworkClientAccessor();
        ClientAccessor.init(networkClientAccessor, new ItemsClientAccessor());
        ClientPlayConnectionEvents.JOIN.register((listener, sender, client) -> sendTrackingPreferences());
        ClientPlayConnectionEvents.DISCONNECT.register((listener, client) -> networkClientAccessor.restoreLocalConfig());
    }

    private static void sendTrackingPreferences()
    {
        var prefs = new TrackingPreferences(
                ClientConfig.AUTO_UPDATE_ON_DISMOUNT.get(),
                ClientConfig.AUTO_UPDATE_ON_INTERACT.get(),
                ClientConfig.AUTO_UPDATE_DISTANCE_THRESHOLD.get());
        new TrackingPreferencesPacket(prefs).sendToServer();
    }
}
