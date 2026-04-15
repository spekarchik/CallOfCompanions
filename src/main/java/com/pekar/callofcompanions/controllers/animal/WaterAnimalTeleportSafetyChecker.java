package com.pekar.callofcompanions.controllers.animal;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public class WaterAnimalTeleportSafetyChecker extends TeleportSafetyCheckerBase
{
    @Override
    public boolean canTeleport(Level level, BlockPos pos)
    {
        // water-animals (future use): ground must be solid and above must be water, next two above water or air
        var below = level.getBlockState(pos.below());
        var at = level.getBlockState(pos);

        boolean belowWater = below.is(Blocks.WATER);
        boolean atIsOk = at.is(Blocks.WATER) || (at.isAir() && belowWater);
        boolean aboveOk = isAirOrWater(level, pos.above());
        boolean above2Ok = isAirOrWater(level, pos.above(2));

        if (!(atIsOk && aboveOk && above2Ok)) return false;

        // every neighbour must have at least 1 block of water above and next 2 above either water or air
        for (int dx = -1; dx <= 1; dx++)
        {
            for (int dz = -1; dz <= 1; dz++)
            {
                if (dx == 0 && dz == 0) continue;

                var neighBelowPos = pos.offset(dx, -1, dz);
                var neighAtPos = pos.offset(dx, 0, dz);
                var neighAbovePos = pos.offset(dx, 1, dz);
                var neighAbove2Pos = pos.offset(dx, 2, dz);

                boolean neighAtIsOk = level.isWaterAt(neighAtPos) || (hasNoCollisions(level, neighAtPos) && level.isWaterAt(neighBelowPos));
                if (!neighAtIsOk) return false;
                if (!isAirOrWater(level, neighAbovePos)) return false;
                if (!isAirOrWater(level, neighAbove2Pos)) return false;
            }
        }

        return true;
    }

    @Override
    public int getMinTeleportYOffset()
    {
        return -2;
    }

    private boolean isAirOrWater(Level level, BlockPos pos)
    {
        return hasNoCollisions(level, pos) || level.isWaterAt(pos);
    }
}
