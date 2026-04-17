package com.pekar.callofcompanions.controllers.animal;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import static com.pekar.callofcompanions.controllers.CallCrystalHelper.hasNoAirCollisions;
import static com.pekar.callofcompanions.controllers.CallCrystalHelper.isLavaSource;

public class StriderTeleportSafetyChecker implements TeleportSafetyChecker
{
    @Override
    public boolean canTeleport(Level level, BlockPos pos)
    {
        var below = level.getBlockState(pos.below());

        boolean atIsOk = isLavaSource(level, pos) || (hasNoAirCollisions(level, pos) && below.isSolidRender());
        boolean aboveOk = hasNoAirCollisions(level, pos.above());
        boolean above2Ok = hasNoAirCollisions(level, pos.above(2));
        boolean above3Ok = hasNoAirCollisions(level, pos.above(3));

        if (!(atIsOk && aboveOk && above2Ok && above3Ok)) return false;

        for (int dx = -1; dx <= 1; dx++)
        {
            for (int dz = -1; dz <= 1; dz++)
            {
                if (dx == 0 && dz == 0) continue;

                var neightAtPos = pos.offset(dx, 0, dz);
                var neighAbovePos = pos.offset(dx, 1, dz);
                var neighAbove2Pos = pos.offset(dx, 2, dz);
                var neighAbove3Pos = pos.offset(dx, 3, dz);

                var neighBelow = level.getBlockState(pos.offset(dx, -1, dz));

                boolean neighAtIsOk = isLavaSource(level, neightAtPos) || (hasNoAirCollisions(level, neightAtPos) && neighBelow.isSolidRender());
                if (!neighAtIsOk) return false;
                if (!hasNoAirCollisions(level, neighAbovePos)) return false;
                if (!hasNoAirCollisions(level, neighAbove2Pos)) return false;
                if (!hasNoAirCollisions(level, neighAbove3Pos)) return false;
            }
        }

        return true;
    }

    @Override
    public int getMinTeleportYOffset()
    {
        return -2;
    }
}
