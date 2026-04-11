package com.pekar.callofcompanions.items;

import com.pekar.callofcompanions.controllers.AnimalSummonController;
import com.pekar.callofcompanions.data.DataRegistry;
import com.pekar.callofcompanions.data.PositionStatus;
import com.pekar.callofcompanions.tooltip.ITooltip;
import com.pekar.callofcompanions.tooltip.ITooltipProvider;
import com.pekar.callofcompanions.tooltip.TextStyle;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class DeepCallCrystal extends CallCrystal implements ITooltipProvider
{
    public DeepCallCrystal(Properties properties)
    {
        super(properties);
    }

    @Override
    protected int crystalCooldown()
    {
        return 400;
    }

    @Override
    protected float callDelayFactor()
    {
        return 1F;
    }

    @Override
    public void addTooltip(ItemStack stack, TooltipContext context, ITooltip tooltip, TooltipFlag flag)
    {
        var companionData = stack.get(DataRegistry.COMPANIONS);
        if (companionData == null) return;

        for (var companion : companionData.companions())
        {
            var name = AnimalSummonController.buildAnimalName(companion.type(), companion.name());
            var status = companion.positionStatus() == PositionStatus.LOST ? "" : "✓";

            tooltip.addLine(getDescriptionId(), 1)
                    .fillWith(name, companion.ownerName(), status)
                    .styledAs(TextStyle.DarkGray, companion.positionStatus() == PositionStatus.LOST)
                    .apply();
        }
    }
}