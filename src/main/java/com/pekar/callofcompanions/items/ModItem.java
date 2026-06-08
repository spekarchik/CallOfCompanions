package com.pekar.callofcompanions.items;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;

public class ModItem extends Item
{
    public ModItem(Properties properties)
    {
        super(properties);
    }

    protected InteractionResult sidedSuccess(boolean isClientSide)
    {
        return InteractionResult.sidedSuccess(isClientSide);
    }
}
