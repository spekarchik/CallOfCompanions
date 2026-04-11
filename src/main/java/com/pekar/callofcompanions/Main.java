package com.pekar.callofcompanions;

import com.mojang.logging.LogUtils;
import com.pekar.callofcompanions.data.DataRegistry;
import com.pekar.callofcompanions.events.EventRegistry;
import com.pekar.callofcompanions.items.ItemRegistry;
import com.pekar.callofcompanions.menus.MenuRegistry;
import com.pekar.callofcompanions.tab.MainTab;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(Main.MODID)
public class Main
{
    public static final String MODID = "callofcompanions";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN_TAB = new MainTab().createTab();
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, MODID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, MODID);


    public Main(IEventBus modEventBus, ModContainer modContainer)
    {
        initializeRegistry();

        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
        DATA_COMPONENTS.register(modEventBus);

        NeoForge.EVENT_BUS.register(this);
        EventRegistry.registerEvents();
        EventRegistry.registerEventsOnModBus(modEventBus);
    }

    private void initializeRegistry()
    {
        ItemRegistry.initStatic();
        DataRegistry.initStatic();
        MenuRegistry.initStatic();
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        // Do something when the server starts
//        LOGGER.info("HELLO from server starting");
    }
}
