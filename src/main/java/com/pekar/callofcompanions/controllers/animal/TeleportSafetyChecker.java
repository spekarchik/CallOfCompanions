package com.pekar.callofcompanions.controllers.animal;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface TeleportSafetyChecker
{
    boolean canTeleport(Level level, BlockPos pos);
    int getMinTeleportYOffset();
}
