package com.pekar.callofcompanions.controllers;

import com.pekar.callofcompanions.data.CompanionData;
import com.pekar.callofcompanions.data.CompanionEntry;
import com.pekar.callofcompanions.data.DataRegistry;
import com.pekar.callofcompanions.items.ItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.nautilus.AbstractNautilus;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

public class CallCrystalHelper
{
    public static boolean hasSameId(ItemStack stack, UUID crystalId)
    {
        if (!stack.is(ItemRegistry.CALL_CRYSTALS_TAG)) return false;

        var id = stack.get(DataRegistry.CRYSTAL_ID);
        return id != null && id.equals(crystalId);
    }

    public static String getAnimalType(Entity entity)
    {
        return entity.getType().getDescription().getString();
    }

    public static boolean canSummonAnimal(Entity entity, Player player)
    {
        if (entity instanceof TamableAnimal tamable && (!tamable.isTame() || !tamable.isOwnedBy(player)))
            return false;

        if (entity instanceof AbstractHorse horse)
        {
            if (horse.isTamed() && horse.getOwner() != null && horse.getOwner() != player) return false;
            return horse.isTamed() || horse.hasCustomName();
        }

        return true;
    }

    public static String buildAnimalName(String animalType, String animalName)
    {
        return animalName.equals(animalType) ? animalType : animalType + " '" + animalName + "'";
    }

    public static void updateCompanionPos(ServerLevel level, CompanionData companions, CompanionEntry companion)
    {
        var entity = level.getEntity(companion.uuid());
        if (entity == null)
        {
            companions.add(companion.getAsLost());
            return;
        }

        var newEntry = companion.getWith(entity.blockPosition(), getAnimalType(entity));
        companions.add(newEntry);
    }

    public static boolean isSafeForTeleleporting(Level level, BlockPos pos, boolean isWaterAnimal)
    {
        var groundPos = pos.below();
        var groundState = level.getBlockState(groundPos);

        // basic invalid ground checks
        if (!groundState.isSolidRender() || groundState.is(BlockTags.FIRE) || groundState.is(Blocks.LAVA))
            return false;

        if (!isWaterAnimal)
        {
            // non-water animals: require 3 blocks of air above target (pos, pos+1, pos+2)
            var at = level.getBlockState(pos);
            var above = level.getBlockState(pos.above());
            var above2 = level.getBlockState(pos.above(2));

            if (!(at.isAir() && above.isAir() && above2.isAir())) return false;

            // check 8 neighbours around the ground block
            for (int dx = -1; dx <= 1; dx++)
            {
                for (int dz = -1; dz <= 1; dz++)
                {
                    if (dx == 0 && dz == 0) continue;

                    var neighGroundPos = groundPos.offset(dx, 0, dz);
                    var neighGroundState = level.getBlockState(neighGroundPos);

                    // Option 1: neighbour ground is solid (and not fire/lava)
                    boolean opt1 = neighGroundState.isSolidRender() &&
                            !neighGroundState.is(BlockTags.FIRE) && !neighGroundState.is(Blocks.LAVA);

                    // Option 2: neighbour ground is air but has a solid block below it (and not fire/lava)
                    var belowNeigh = level.getBlockState(neighGroundPos.below());
                    boolean opt2 = neighGroundState.isAir() && belowNeigh.isSolidRender() &&
                            !belowNeigh.is(BlockTags.FIRE) && !belowNeigh.is(Blocks.LAVA);

                    if (!(opt1 || opt2)) return false;

                    // and ensure 3 blocks of air above neighbour column (pos offset at same x/z)
                    var neighAt = level.getBlockState(pos.offset(dx, 0, dz));
                    var neighAbove = level.getBlockState(pos.offset(dx, 1, dz));
                    var neighAbove2 = level.getBlockState(pos.offset(dx, 2, dz));

                    if (!(neighAt.isAir() && neighAbove.isAir() && neighAbove2.isAir())) return false;
                }
            }

            return true;
        }
        else
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
    }

    public static boolean isSafeForDestPoint(Level level, BlockPos pos)
    {
        var below = level.getBlockState(pos.below());
        var at = level.getBlockState(pos);
        var above = level.getBlockState(pos.above());
        var above2 = level.getBlockState(pos.above(2));

        return below.isSolidRender() &&
                !below.is(BlockTags.FIRE) &&
                !below.is(Blocks.LAVA) &&
                isAirOrWater(at) &&                    // body
                isAirOrWater(above) && isAirOrWater(above2); // head
    }

    private static boolean isAirOrWater(BlockState state)
    {
        return state.isAir() || state.is(Blocks.WATER);
    }
}
