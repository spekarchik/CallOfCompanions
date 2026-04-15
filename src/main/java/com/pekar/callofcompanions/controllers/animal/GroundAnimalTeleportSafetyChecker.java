package com.pekar.callofcompanions.controllers.animal;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public class GroundAnimalTeleportSafetyChecker extends TeleportSafetyCheckerBase
{
    @Override
    public boolean canTeleport(Level level, BlockPos pos)
    {
        var groundPos = pos.below();
        var groundState = level.getBlockState(groundPos);

        // basic invalid ground checks
        if (!groundState.isSolidRender(level, groundPos) || groundState.is(BlockTags.FIRE) || groundState.is(Blocks.LAVA))
            return false;

        // non-water animals: require 3 blocks of air above target (pos, pos+1, pos+2)
        if (!(hasNoCollisions(level, pos) && hasNoCollisions(level, pos.above()) && hasNoCollisions(level, pos.above(2)))) return false;

        // check 8 neighbours around the ground block
        for (int dx = -1; dx <= 1; dx++)
        {
            for (int dz = -1; dz <= 1; dz++)
            {
                if (dx == 0 && dz == 0) continue;

                var neighGroundPos = groundPos.offset(dx, 0, dz);
                var neighGroundState = level.getBlockState(neighGroundPos);

                // Option 1: neighbor ground is solid (and not fire/lava)
                boolean opt1 = neighGroundState.isSolidRender(level, neighGroundPos) &&
                        !neighGroundState.is(BlockTags.FIRE) && !neighGroundState.is(Blocks.LAVA);

                // Option 2: neighbor ground is air but has a solid block below it (and not fire/lava)
                var belowNeigh = level.getBlockState(neighGroundPos.below());
                boolean opt2 = hasNoCollisions(level, neighGroundPos) && belowNeigh.isSolidRender(level, neighGroundPos.below()) &&
                        !belowNeigh.is(BlockTags.FIRE) && !belowNeigh.is(Blocks.LAVA);

                if (!(opt1 || opt2)) return false;

                // and ensure 3 blocks of air above neighbour column (pos offset at same x/z)
                var neighAt = pos.offset(dx, 0, dz);
                var neighAbove = pos.offset(dx, 1, dz);
                var neighAbove2 = pos.offset(dx, 2, dz);

                if (!(hasNoCollisions(level, neighAt) && hasNoCollisions(level, neighAbove) && hasNoCollisions(level, neighAbove2))) return false;
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
