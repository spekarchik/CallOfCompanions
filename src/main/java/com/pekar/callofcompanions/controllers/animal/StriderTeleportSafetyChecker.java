package com.pekar.callofcompanions.controllers.animal;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public class StriderTeleportSafetyChecker extends TeleportSafetyCheckerBase
{
    @Override
    public boolean canTeleport(Level level, BlockPos pos)
    {
        var below = level.getBlockState(pos.below());
        var at = level.getBlockState(pos);
        var above = level.getBlockState(pos.above());
        var above2 = level.getBlockState(pos.above(2));
        var above3 = level.getBlockState(pos.above(3));

        boolean atIsOk = at.is(Blocks.LAVA) || (at.isAir() && below.isSolidRender());
        boolean aboveOk = above.isAir();
        boolean above2Ok = above2.isAir();
        boolean above3Ok = above3.isAir();

        if (!(atIsOk && aboveOk && above2Ok && above3Ok)) return false;

        for (int dx = -1; dx <= 1; dx++)
        {
            for (int dz = -1; dz <= 1; dz++)
            {
                if (dx == 0 && dz == 0) continue;

                var neighBelow = level.getBlockState(pos.offset(dx, -1, dz));
                var neighAt = level.getBlockState(pos.offset(dx, 0, dz));
                var neighAbove = level.getBlockState(pos.offset(dx, 1, dz));
                var neighAbove2 = level.getBlockState(pos.offset(dx, 2, dz));
                var neighAbove3 = level.getBlockState(pos.offset(dx, 3, dz));

                boolean neighAtIsOk = neighAt.is(Blocks.LAVA) || (neighAt.isAir() && neighBelow.isSolidRender());
                if (!neighAtIsOk) return false;
                if (!neighAbove.isAir()) return false;
                if (!neighAbove2.isAir()) return false;
                if (!neighAbove3.isAir()) return false;
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
