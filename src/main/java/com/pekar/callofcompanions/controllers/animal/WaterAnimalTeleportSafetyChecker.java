package com.pekar.callofcompanions.controllers.animal;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import static com.pekar.callofcompanions.controllers.CallCrystalHelper.isWaterSource;
import static com.pekar.callofcompanions.controllers.CallCrystalHelper.noCollisionOrIsWater;

public class WaterAnimalTeleportSafetyChecker implements TeleportSafetyChecker
{
    @Override
    public boolean canTeleport(Level level, BlockPos pos)
    {
        boolean atIsOk = isWaterSource(level, pos);
        boolean aboveOk = noCollisionOrIsWater(level, pos.above());
        boolean above2Ok = noCollisionOrIsWater(level, pos.above(2));

        if (!(atIsOk && aboveOk && above2Ok)) return false;

        // every neighbor must have at least 1 block of water above and next 2 above either water or air
        for (int dx = -1; dx <= 1; dx++)
        {
            for (int dz = -1; dz <= 1; dz++)
            {
                if (dx == 0 && dz == 0) continue;

                var neighAtPos = pos.offset(dx, 0, dz);
                var neighAbovePos = pos.offset(dx, 1, dz);
                var neighAbove2Pos = pos.offset(dx, 2, dz);

                boolean neighAtIsOk = isWaterSource(level, neighAtPos);
                if (!neighAtIsOk) return false;
                if (!noCollisionOrIsWater(level, neighAbovePos)) return false;
                if (!noCollisionOrIsWater(level, neighAbove2Pos)) return false;
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
