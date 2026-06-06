package com.pekar.callofcompanions.events.fabric;

import com.pekar.callofcompanions.events.CustomizationEvents;
import com.pekar.callofcompanions.events.params.PlayerEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

public final class FabricCustomizationEventHooks
{
    private static final CustomizationEvents CUSTOMIZATION_EVENTS = new CustomizationEvents();
    private static boolean initialized = false;

    private FabricCustomizationEventHooks()
    {}

    public static void init()
    {
        if (initialized) return;
        initialized = true;
    }

    public static void onItemCrafted(ServerPlayer player, ItemStack crafting, Container inventory)
    {
        CUSTOMIZATION_EVENTS.onItemCraftedEvent(new PlayerEvent.ItemCraftedEvent(player, crafting, inventory));
    }
}

