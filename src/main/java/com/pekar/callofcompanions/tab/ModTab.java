package com.pekar.callofcompanions.tab;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.Collection;

import static com.pekar.callofcompanions.Main.CREATIVE_MODE_TABS;

public abstract class ModTab
{
    protected abstract String getTabName();

    protected abstract ItemStack getIconItem();

    protected abstract Collection<Item> getTabItems();

    protected abstract ResourceKey<CreativeModeTab>[] getTabsBefore();

    protected String getTitle()
    {
        return "itemGroup." + getTabName();
    }

    public final DeferredHolder<CreativeModeTab, CreativeModeTab> createTab()
    {

        return CREATIVE_MODE_TABS.register(getTabName(), () -> CreativeModeTab.builder()
                .title(Component.translatable(getTitle()))
                .withTabsBefore(getTabsBefore())
                .icon(this::getIconItem)
                .displayItems(this::addItems)
                .build());
    }

    private void addItems(CreativeModeTab.ItemDisplayParameters itemDisplayParameters, CreativeModeTab.Output output)
    {
        for (var item : getTabItems())
        {
            output.accept(item);
        }
    }
}
