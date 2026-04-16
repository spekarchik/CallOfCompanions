package com.pekar.callofcompanions.controllers.animal;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import static com.pekar.callofcompanions.controllers.CallCrystalHelper.hasNoAirCollisions;

public class GroundAnimalTeleportSafetyChecker implements TeleportSafetyChecker
{
    @Override
    public boolean canTeleport(Level level, BlockPos pos)
    {
        var groundPos = pos.below();
        var groundState = level.getBlockState(groundPos);

        if (!groundState.isSolidRender()) return false;

        // non-water animals: require 3 blocks of air above target (pos, pos+1, pos+2)
        if (!(hasNoAirCollisions(level, pos) && hasNoAirCollisions(level, pos.above()) && hasNoAirCollisions(level, pos.above(2)))) return false;

        // check 8 neighbors around the ground block
        for (int dx = -1; dx <= 1; dx++)
        {
            for (int dz = -1; dz <= 1; dz++)
            {
                if (dx == 0 && dz == 0) continue;

                var neighGroundPos = pos.offset(dx, -1, dz);
                var neighGroundState = level.getBlockState(neighGroundPos);

                // Option 1: neighbor ground is solid (and not fire/lava)
                boolean opt1 = neighGroundState.isSolidRender();

                // Option 2: neighbor ground is air but has a solid block below it (and not fire/lava)
                var belowNeigh = level.getBlockState(neighGroundPos.below());
                boolean opt2 = hasNoAirCollisions(level, neighGroundPos) && belowNeigh.isSolidRender();

                if (!(opt1 || opt2)) return false;

                // and ensure 3 blocks of air above neighbor column (pos offset at same x/z)
                var neighAt = pos.offset(dx, 0, dz);
                var neighAbove = pos.offset(dx, 1, dz);
                var neighAbove2 = pos.offset(dx, 2, dz);

                if (!(hasNoAirCollisions(level, neighAt) && hasNoAirCollisions(level, neighAbove) && hasNoAirCollisions(level, neighAbove2))) return false;
            }
        }

        return true;
    }

    @Override
    public int getMinTeleportYOffset()
    {
        return 0;
    }
}
