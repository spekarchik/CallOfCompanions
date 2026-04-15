package com.pekar.callofcompanions.controllers.animal;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class WaterAnimalTeleportSafetyChecker extends TeleportSafetyCheckerBase
{
    @Override
    public boolean canTeleport(Level level, BlockPos pos)
    {
        // water-animals (future use): ground must be solid and above must be water, next two above water or air
        var below = level.getBlockState(pos.below());
        var at = level.getBlockState(pos);
        var above = level.getBlockState(pos.above());
        var above2 = level.getBlockState(pos.above(2));

        boolean belowWater = below.is(Blocks.WATER);
        boolean atIsOk = at.is(Blocks.WATER) || (at.isAir() && belowWater);
        boolean aboveOk = isAirOrWater(above);
        boolean above2Ok = isAirOrWater(above2);

        if (!(atIsOk && aboveOk && above2Ok)) return false;

        // every neighbour must have at least 1 block of water above and next 2 above either water or air
        for (int dx = -1; dx <= 1; dx++)
        {
            for (int dz = -1; dz <= 1; dz++)
            {
                if (dx == 0 && dz == 0) continue;

                var neighBelow = level.getBlockState(pos.offset(dx, -1, dz));
                var neighAt = level.getBlockState(pos.offset(dx, 0, dz));
                var neighAbove = level.getBlockState(pos.offset(dx, 1, dz));
                var neighAbove2 = level.getBlockState(pos.offset(dx, 2, dz));

                boolean neighAtIsOk = neighAt.is(Blocks.WATER) || (neighAt.isAir() && neighBelow.is(Blocks.WATER));
                if (!neighAtIsOk) return false;
                if (!isAirOrWater(neighAbove)) return false;
                if (!isAirOrWater(neighAbove2)) return false;
            }
        }

        return true;
    }

    @Override
    public int getMinTeleportYOffset()
    {
        return -2;
    }

    private static boolean isAirOrWater(BlockState state)
    {
        return state.isAir() || state.is(Blocks.WATER);
    }
}
