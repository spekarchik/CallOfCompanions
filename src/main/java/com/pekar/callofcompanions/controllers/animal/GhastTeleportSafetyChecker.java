package com.pekar.callofcompanions.controllers.animal;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public class GhastTeleportSafetyChecker extends TeleportSafetyCheckerBase
{
    @Override
    public boolean canTeleport(Level level, BlockPos pos)
    {
        var groundPos = pos.below();
        var groundState = level.getBlockState(groundPos);

        // basic invalid ground checks
        if (groundState.is(BlockTags.FIRE) || groundState.is(Blocks.LAVA))
            return false;

        // require a 6x6x6 air volume above the target pos
        // x and z range: -2..+3 (6 blocks), y range: 0..5 (6 blocks: pos .. pos+5)
        for (int dx = -2; dx <= 3; dx++)
        {
            for (int dz = -2; dz <= 3; dz++)
            {
                for (int dy = 0; dy <= 5; dy++)
                {
                    var checkPos = pos.offset(dx, dy, dz);
                    if (!hasNoCollisions(level, checkPos)) return false;
                }
            }
        }

        // check 8 neighbors around the ground block (keep original ground neighbour validation)
        for (int dx = -1; dx <= 1; dx++)
        {
            for (int dz = -1; dz <= 1; dz++)
            {
                if (dx == 0 && dz == 0) continue;

                var neighGroundPos = groundPos.offset(dx, 0, dz);
                var neighGroundState = level.getBlockState(neighGroundPos);

                // Option 1: neighbor ground is solid (and not fire/lava)
                boolean opt1 = !neighGroundState.is(BlockTags.FIRE) && !neighGroundState.is(Blocks.LAVA);

                // Option 2: neighbor ground is air but has a solid block below it (and not fire/lava)
                var belowNeigh = level.getBlockState(neighGroundPos.below());
                boolean opt2 = hasNoCollisions(level, neighGroundPos) &&
                        !belowNeigh.is(BlockTags.FIRE) && !belowNeigh.is(Blocks.LAVA);

                if (!(opt1 || opt2)) return false;
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
