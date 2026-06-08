package com.pekar.callofcompanions.tooltip;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public interface ITooltipProvider
{
    void addTooltip(ItemStack stack, Item.TooltipContext context, ITooltip tooltip, TooltipFlag flag);

    static void appendHoverText(ITooltipProvider tooltipProvider, ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag flag)
    {
        tooltipProvider.addTooltip(stack, context, Tooltip.create(tooltipComponents::add), flag);
    }
}
