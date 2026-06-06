package com.pekar.callofcompanions.tab;

import com.pekar.callofcompanions.items.ItemRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Collection;

public class MainTab extends ModTab
{
    @Override
    protected String getTabName()
    {
        return "main_tab";
    }

    @Override
    protected ItemStack getIconItem()
    {
        return new ItemStack(ItemRegistry.CALL_CRYSTAL);
    }

    @Override
    protected Collection<Item> getTabItems()
    {
        return List.of(
                ItemRegistry.CALL_CRYSTAL,
                ItemRegistry.DEEP_CALL_CRYSTAL
        );
    }

    @Override
    protected ResourceKey<CreativeModeTab>[] getTabsBefore()
    {
        return new ResourceKey[]
                {
                        CreativeModeTabs.SPAWN_EGGS
                };
    }
}
