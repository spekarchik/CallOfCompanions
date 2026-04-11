package com.pekar.callofcompanions.utils;

import com.pekar.callofcompanions.tooltip.ITooltip;
import net.minecraft.world.item.TooltipFlag;

public class Text
{
    Text()
    {

    }

    public static boolean showExtendedDescription(ITooltip tooltip, TooltipFlag flag)
    {
        if (!flag.hasShiftDown())
        {
            tooltip.addLineById("description.press_shift").apply();
            return false;
        }

        return true;
    }
}
