package com.pekar.callofcompanions;

import com.mojang.logging.LogUtils;
import com.pekar.callofcompanions.data.DataRegistry;
import com.pekar.callofcompanions.items.ItemRegistry;
import com.pekar.callofcompanions.tab.MainTab;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;

import java.io.IOException;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
//@Mod(Main.MODID)
public class Main implements ModInitializer
{
    public static final String MODID = "callofcompanions";
    public static final Logger LOGGER = LogUtils.getLogger();
//    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
//    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
//    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN_TAB = new MainTab().createTab();
//    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, MODID);
//    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, MODID);

    @Override
    public void onInitialize()
    {
        var configPath = FabricLoader.getInstance()
                .getConfigDir()
                .resolve("enchantonce-common.toml");

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
    }

//    public Main(IEventBus modEventBus, ModContainer modContainer)
//    {
//        initializeRegistry();
//
//        ITEMS.register(modEventBus);
//        CREATIVE_MODE_TABS.register(modEventBus);
//        DATA_COMPONENTS.register(modEventBus);
//
//        NeoForge.EVENT_BUS.register(this);
//        EventRegistry.registerEvents();
//        EventRegistry.registerEventsOnModBus(modEventBus);
//
//        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
//    }

//    private void initializeRegistry()
//    {
//        ItemRegistry.initStatic();
//        DataRegistry.initStatic();
//    }
}
