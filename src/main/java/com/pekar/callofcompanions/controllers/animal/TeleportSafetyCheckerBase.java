package com.pekar.callofcompanions.controllers.animal;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

abstract class TeleportSafetyCheckerBase implements TeleportSafetyChecker
{
    protected boolean hasNoCollisions(Level level, BlockPos pos)
    {
        var state = level.getBlockState(pos);
        var collisionShape = state.getCollisionShape(level, pos);
        return collisionShape.isEmpty();
    }
}
