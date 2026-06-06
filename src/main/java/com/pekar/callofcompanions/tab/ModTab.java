package com.pekar.callofcompanions.tab;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Collection;

import static com.pekar.callofcompanions.Main.MODID;
import static com.pekar.callofcompanions.utils.Resources.createResourceLocation;

public abstract class ModTab
{
    protected abstract String getTabName();

    protected abstract ItemStack getIconItem();

    protected abstract Collection<Item> getTabItems();

    protected String getTitle()
    {
        return "itemGroup." + getTabName();
    }

    public final CreativeModeTab createTab()
    {
        return Registry.register(
                BuiltInRegistries.CREATIVE_MODE_TAB,
                createResourceLocation(MODID, getTabName()),
                CreativeModeTab.builder(CreativeModeTab.Row.TOP, 7)
                        .title(Component.translatable(getTitle()))
                        .icon(this::getIconItem)
                        .displayItems(this::addItems)
                        .build()
        );
    }

    private void addItems(CreativeModeTab.ItemDisplayParameters itemDisplayParameters, CreativeModeTab.Output output)
    {
        for (var item : getTabItems())
        {
            output.accept(item);
        }
    }
}
