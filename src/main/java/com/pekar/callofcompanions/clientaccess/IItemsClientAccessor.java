package com.pekar.callofcompanions.clientaccess;

import net.minecraft.world.level.Level;

public interface IItemsClientAccessor
{
    Level getLevel();
    boolean hasShiftDown();
    boolean hasAltDown();
}
