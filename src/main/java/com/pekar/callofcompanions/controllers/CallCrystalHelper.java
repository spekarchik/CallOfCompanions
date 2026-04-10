package com.pekar.callofcompanions.controllers;

import com.pekar.callofcompanions.data.DataRegistry;
import com.pekar.callofcompanions.items.ItemRegistry;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public class CallCrystalHelper
{
    public static boolean hasSameId(ItemStack stack, UUID crystalId)
    {
        if (!stack.is(ItemRegistry.CALL_CRYSTAL)) return false;

        var id = stack.get(DataRegistry.CRYSTAL_ID);
        return id != null && id.equals(crystalId);
    }
}
