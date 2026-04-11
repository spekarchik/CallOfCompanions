package com.pekar.callofcompanions.items;

import com.pekar.callofcompanions.tooltip.ITooltipProvider;

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
}