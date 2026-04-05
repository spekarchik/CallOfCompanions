package com.pekar.callofcompanions.items;

import com.pekar.callofcompanions.data.DataRegistry;
import com.pekar.callofcompanions.tooltip.ITooltip;
import com.pekar.callofcompanions.tooltip.ITooltipProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

public class CallCrystal extends ModItem implements ITooltipProvider
{
    public CallCrystal(Properties properties)
    {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag tooltipFlag)
    {
        ITooltipProvider.appendHoverText(this, itemStack, context, display, builder, tooltipFlag);
    }

    @Override
    public void addTooltip(ItemStack stack, TooltipContext context, ITooltip tooltip, TooltipFlag flag)
    {
        var companions = stack.get(DataRegistry.COMPANIONS);
        if (companions == null) return;

        System.out.println("# " + companions.getCompanions().size());
        for (var companion : companions.getCompanions())
        {
            var name = buildName(companion.type(), companion.name());
            tooltip.addLine(getDescriptionId(), 1)
                    .fillWith(name, companion.ownerName())
                    .apply();
        }
    }

    private String buildName(String animalType, String animalName)
    {
        return animalName.equals(animalType) ? animalType : animalType + " '" + animalName + "'";
    }
}
