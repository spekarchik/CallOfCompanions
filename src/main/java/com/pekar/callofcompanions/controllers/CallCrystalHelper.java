package com.pekar.callofcompanions.controllers;

import com.pekar.callofcompanions.blocks.BlockRegistry;
import com.pekar.callofcompanions.data.CompanionData;
import com.pekar.callofcompanions.data.CompanionEntry;
import com.pekar.callofcompanions.data.DataRegistry;
import com.pekar.callofcompanions.items.ItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
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

    public static boolean canSummonAnimal(Entity entity, UUID entityOwnerId, Player player)
    {
        if (entity == null && entityOwnerId != null && !entityOwnerId.equals(player.getUUID()))
            return false;

        if (entity instanceof TamableAnimal tamable)
        {
            if (!tamable.isTame() && !tamable.hasCustomName()) return false;
            var ownerId = tamable.getOwnerUUID();
            if (ownerId != null && !ownerId.equals(player.getUUID())) return false; // don't rely on `tamable.isOwnedBy(player)`!
        }

        if (entity instanceof AbstractHorse horse)
        {
            if (horse.isTamed() && horse.getOwnerUUID() != null && !horse.getOwnerUUID().equals(player.getUUID())) return false;
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

        var newEntry = companion.getWith(entity.level().dimension(), entity.blockPosition(), getAnimalType(entity), level);
        companions.add(newEntry);
    }

    public static boolean canApplyCrystalAt(Level level, BlockPos pos)
    {
        var below = level.getBlockState(pos.below());

        return (below.isCollisionShapeFullBlock(level, pos.below()) || below.is(BlockRegistry.CALL_CRYSTAL_NOT_FULL_USABLE_TAG)) &&
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
        return (state.isCollisionShapeFullBlock(level, pos) || state.is(BlockRegistry.CALL_CRYSTAL_NOT_FULL_USABLE_TAG))
                && !state.is(Blocks.MAGMA_BLOCK);
    }
}
