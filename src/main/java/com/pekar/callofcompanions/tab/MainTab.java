package com.pekar.callofcompanions.tab;

import com.pekar.callofcompanions.Main;
import com.pekar.callofcompanions.items.ItemRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Collection;
import java.util.stream.Collectors;

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
        return ItemRegistry.CALL_CRYSTAL.toStack();
    }

    @Override
    protected Collection<Item> getTabItems()
    {
        //ArmorRegistry.initStatic();

        return Main.ITEMS.getEntries().stream().map(x -> x.get()).collect(Collectors.toList()); // block items are also included
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
