package com.pekar.callofcompanions;

import com.pekar.callofcompanions.client.NetworkClientAccessor;
import com.pekar.callofcompanions.clientaccess.ClientAccessor;
import com.pekar.callofcompanions.data.TrackingPreferences;
import com.pekar.callofcompanions.network.TrackingPreferencesPacket;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = Main.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = Main.MODID, value = Dist.CLIENT)
public class ClientMain
{
    public ClientMain(ModContainer container)
    {
        ConfigMigration.migrate(FMLPaths.CONFIGDIR.get(), "client");
        container.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);

        // Allows NeoForge to create a config screen for this mod's configs.
        // The config screen is accessed by going to the Mods screen > clicking on your mod > clicking on config.
        // Do not forget to add translations for your config options to the en_us.json file.
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onLogin(ClientPlayerNetworkEvent.LoggingIn event)
    {
        sendTrackingPreferences();
    }

    @SubscribeEvent
    static void onConfigReload(ModConfigEvent.Reloading event)
    {
        if (event.getConfig().getSpec() != ClientConfig.SPEC) return;
        // File reloads may originate outside the client thread.
        Minecraft.getInstance().execute(ClientMain::sendTrackingPreferences);
    }

    private static void sendTrackingPreferences()
    {
        if (Minecraft.getInstance().getConnection() == null) return;

        var prefs = new TrackingPreferences(
                ClientConfig.AUTO_UPDATE_ON_DISMOUNT.get(),
                ClientConfig.AUTO_UPDATE_ON_INTERACT.get(),
                ClientConfig.AUTO_UPDATE_DISTANCE_THRESHOLD.get());

        new TrackingPreferencesPacket(prefs).sendToServer();
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event)
    {
        ClientAccessor.init(new NetworkClientAccessor());
    }
}
