package com.pekar.callofcompanions.items;

import com.pekar.callofcompanions.Config;
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

    @Override
    protected String getSummonableAnimalsDescriptionId()
    {
        return Config.DEEP_CRYSTAL_DISALLOW_UNTAMED.isTrue()
                ? "item.callofcompanions.call_crystal"
                : getDescriptionId();
    }

    @Override
    protected int crystalDataCapacity()
    {
        return Config.DEEP_CRYSTAL_DATA_CAPACITY.getAsInt();
    }
}