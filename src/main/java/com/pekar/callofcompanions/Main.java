package com.pekar.callofcompanions;

import com.mojang.logging.LogUtils;
import com.pekar.callofcompanions.data.DataRegistry;
import com.pekar.callofcompanions.events.fabric.FabricAnimalEventHooks;
import com.pekar.callofcompanions.events.fabric.FabricCustomizationEventHooks;
import com.pekar.callofcompanions.events.fabric.FabricLifecycleEventHooks;
import com.pekar.callofcompanions.events.fabric.FabricPlayerEventHooks;
import com.pekar.callofcompanions.items.ItemRegistry;
import com.pekar.callofcompanions.network.Networking;
import com.pekar.callofcompanions.network.ServerConfigSyncPacket;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.world.level.storage.LevelResource;
import com.pekar.callofcompanions.tab.MainTab;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main implements ModInitializer
{
    public static final String MODID = "callofcompanions";
    public static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void onInitialize()
    {
        var configDirectory = FabricLoader.getInstance().getConfigDir();
        ConfigMigration.migrate(configDirectory, "server");
        loadServerConfig(configDirectory.resolve(MODID + "-server.toml"));

        DataRegistry.initStatic();
        ItemRegistry.initStatic();
        new MainTab().createTab();

        Networking.init();
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            loadServerConfig(configDirectory.resolve(MODID + "-server.toml"));
            var worldConfig = server.getWorldPath(LevelResource.ROOT).resolve("serverconfig").resolve(MODID + "-server.toml");
            if (Files.exists(worldConfig)) loadServerConfig(worldConfig);
        });
        ServerPlayConnectionEvents.JOIN.register((listener, sender, server) ->
                new ServerConfigSyncPacket(ServerConfig.SPEC.snapshot()).sendToPlayer(listener.player));

        FabricAnimalEventHooks.init();
        FabricCustomizationEventHooks.init();
        FabricLifecycleEventHooks.init();
        FabricPlayerEventHooks.init();
    }

    private static void loadServerConfig(Path path)
    {
        try
        {
            ServerConfig.SPEC.load(path);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to load server config: " + path, e);
        }
    }
}
