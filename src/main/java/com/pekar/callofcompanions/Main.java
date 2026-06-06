package com.pekar.callofcompanions;

import com.mojang.logging.LogUtils;
import com.pekar.callofcompanions.data.DataRegistry;
import com.pekar.callofcompanions.events.fabric.FabricAnimalEventHooks;
import com.pekar.callofcompanions.events.fabric.FabricCustomizationEventHooks;
import com.pekar.callofcompanions.events.fabric.FabricLifecycleEventHooks;
import com.pekar.callofcompanions.events.fabric.FabricPlayerEventHooks;
import com.pekar.callofcompanions.items.ItemRegistry;
import com.pekar.callofcompanions.network.Networking;
import com.pekar.callofcompanions.tab.MainTab;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;

import java.io.IOException;

public class Main implements ModInitializer
{
    public static final String MODID = "callofcompanions";
    public static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void onInitialize()
    {
        var configPath = FabricLoader.getInstance()
                .getConfigDir()
                .resolve(MODID + "-common.toml");

        try
        {
            Config.SPEC.load(configPath);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to load config", e);
        }

        DataRegistry.initStatic();
        ItemRegistry.initStatic();
        new MainTab().createTab();

        Networking.init();

        FabricAnimalEventHooks.init();
        FabricCustomizationEventHooks.init();
        FabricLifecycleEventHooks.init();
        FabricPlayerEventHooks.init();
    }
}
