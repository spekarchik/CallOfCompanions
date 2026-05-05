package com.pekar.callofcompanions.controllers;

import com.pekar.callofcompanions.data.CompanionData;
import com.pekar.callofcompanions.data.CompanionEntry;
import com.pekar.callofcompanions.data.DataRegistry;
import com.pekar.callofcompanions.items.ItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import java.util.UUID;

import static net.minecraft.world.level.material.Fluids.LAVA;
import static net.minecraft.world.level.material.Fluids.WATER;

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

        var newEntry = companion.getWith(entity.level().dimension(), entity.blockPosition(), getAnimalType(entity));
        companions.add(newEntry);
    }

    public static boolean canApplyCrystalAt(Level level, BlockPos pos)
    {
        var below = level.getBlockState(pos.below());

        return below.isCollisionShapeFullBlock(level, pos) &&
                noCollisionOrIsWater(level, pos) &&                    // body
                noCollisionOrIsWater(level, pos.above()) && noCollisionOrIsWater(level, pos.above(2)); // head
    }

    public static boolean hasNoAirCollisions(Level level, BlockPos pos)
    {
        var state = level.getBlockState(pos);
        if (state.getFluidState().is(FluidTags.WATER)
                || state.getFluidState().is(FluidTags.LAVA)
                || state.is(Blocks.FIRE)
                || state.is(Blocks.SOUL_FIRE)
                || state.is(Blocks.SWEET_BERRY_BUSH)
                || state.is(Blocks.POWDER_SNOW))
            return false;

        var collisionShape = state.getCollisionShape(level, pos);
        return collisionShape.isEmpty();
    }

    public static boolean noCollisionOrIsWater(Level level, BlockPos pos)
    {
        return hasNoAirCollisions(level, pos) || isWaterSource(level, pos);
    }

    public static boolean isWaterSource(Level level, BlockPos pos)
    {
        return level.getFluidState(pos).isSourceOfType(WATER);
    }

    public static boolean isLavaSource(Level level, BlockPos pos)
    {
        return level.getFluidState(pos).isSourceOfType(LAVA);
    }

    public static boolean isSafeSolidBlock(Level level, BlockPos pos)
    {
        var state = level.getBlockState(pos);
        return state.isSolidRender() && !state.is(Blocks.MAGMA_BLOCK);
    }
}
