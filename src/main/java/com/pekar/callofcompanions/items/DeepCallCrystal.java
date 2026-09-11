package com.pekar.callofcompanions.items;

import com.pekar.callofcompanions.ServerConfig;
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
        return ServerConfig.DEEP_CRYSTAL_COOLDOWN.getAsInt();
    }

    @Override
    protected float callDelayFactor()
    {
        return 1F;
    }

    @Override
    protected String getSummonableAnimalsInfoDescriptionId()
    {
        return ServerConfig.DEEP_CRYSTAL_ALLOW_UNTAMED.isFalse()
                ? "item.callofcompanions.call_crystal"
                : getDescriptionId();
    }

    @Override
    protected String getCrossDimensionCallsInfoDescriptionId()
    {
        return ServerConfig.DEEP_CRYSTAL_ALLOW_INTERDIMENSIONAL.isFalse()
                ? "item.callofcompanions.call_crystal"
                : getDescriptionId();
    }

    @Override
    protected int crystalDataCapacity()
    {
        return ServerConfig.DEEP_CRYSTAL_DATA_CAPACITY.getAsInt();
    }

    @Override
    protected boolean allowInterDimensionalTeleports()
    {
        return ServerConfig.DEEP_CRYSTAL_ALLOW_INTERDIMENSIONAL.isTrue();
    }

    @Override
    protected int requiredXpAmountToCall()
    {
        if (ServerConfig.CONSUME_XP_ON_CALL.isFalse()) return 0;

        return ServerConfig.DEEP_CRYSTAL_ALLOW_INTERDIMENSIONAL.isTrue()
                ? ServerConfig.XP_LEVELS_TO_CONSUME_CROSS_DIMENSION.getAsInt()
                : ServerConfig.XP_LEVELS_TO_CONSUME.getAsInt();
    }
}