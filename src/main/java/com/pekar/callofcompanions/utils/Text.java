package com.pekar.callofcompanions.utils;

import com.mojang.blaze3d.platform.InputConstants;
import com.pekar.callofcompanions.tooltip.ITooltip;
import net.minecraft.world.item.TooltipFlag;

public class Text
{
    Text()
    {

    }

    public static boolean showExtendedDescription(ITooltip tooltip, TooltipFlag flag)
    {
        if (!hasShiftDown())
        {
            tooltip.addLineById("description.press_shift").apply();
            return false;
        }

        return true;
    }

    private static boolean hasShiftDown()
    {
        return InputConstants.isKeyDown(InputConstants.KEY_LSHIFT)
                || InputConstants.isKeyDown(InputConstants.KEY_RSHIFT);
    }
}
