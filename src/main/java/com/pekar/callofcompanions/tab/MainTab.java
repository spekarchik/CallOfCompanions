package com.pekar.callofcompanions.tab;

import com.pekar.callofcompanions.items.ItemRegistry;
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

}
