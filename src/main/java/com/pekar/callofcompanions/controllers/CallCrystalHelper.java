package com.pekar.callofcompanions.controllers;

import com.pekar.callofcompanions.Config;
import com.pekar.callofcompanions.blocks.BlockRegistry;
import com.pekar.callofcompanions.data.CompanionData;
import com.pekar.callofcompanions.data.CompanionEntry;
import com.pekar.callofcompanions.data.DataRegistry;
import com.pekar.callofcompanions.data.PositionStatus;
import com.pekar.callofcompanions.items.ItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import java.util.Optional;
import java.util.UUID;

import static net.minecraft.world.level.material.Fluids.LAVA;
import static net.minecraft.world.level.material.Fluids.WATER;

public class CallCrystalHelper
{
    public static boolean canBindAnimal(PathfinderMob animal, boolean allowNamedUntamed)
    {
        if (!canBindEntityType(animal)) return false;
        boolean isTameAnimal = animal instanceof TamableAnimal tamable && tamable.isTame();
        boolean isTamedHorse = animal instanceof AbstractHorse horse && horse.isTamed();
        return isTameAnimal || isTamedHorse || (allowNamedUntamed && animal.hasCustomName());
    }

    public static boolean canBindEntityType(Entity entity)
    {
        if (entity instanceof Enemy || entity instanceof Bucketable || entity instanceof AbstractVillager) return false;
        return entity instanceof PathfinderMob;
    }

    public static short crystalDataCapacity(boolean isDeepCallCrystal)
    {
        return isDeepCallCrystal
                ? (short) Config.DEEP_CRYSTAL_DATA_CAPACITY.getAsInt()
                : (short) Config.CRYSTAL_DATA_CAPACITY.getAsInt();
    }

    public static UUID ensureCrystalId(ItemStack stack)
    {
        var crystalId = stack.get(DataRegistry.CRYSTAL_ID);
        if (crystalId != null) return crystalId;

        crystalId = UUID.randomUUID();
        stack.set(DataRegistry.CRYSTAL_ID, crystalId);
        stack.set(DataComponents.MAX_STACK_SIZE, 1);
        return crystalId;
    }

    public static CompanionEntry createCompanionEntry(PathfinderMob animal, long gameTime)
    {
        var name = animal.getDisplayName().getString();
        var companionType = getAnimalType(animal);
        var owner = animal instanceof OwnableEntity ownable ? ownable.getOwner() : null;
        var ownerId = animal instanceof OwnableEntity ownable && ownable.getOwnerReference() != null
                ? ownable.getOwnerReference().getUUID()
                : null;

        return new CompanionEntry(
                animal.getUUID(),
                name,
                companionType,
                animal.level().dimension(),
                animal.blockPosition(),
                PositionStatus.FRESH,
                Optional.ofNullable(ownerId),
                owner != null ? Optional.of(owner.getDisplayName().getString()) : Optional.empty(),
                System.currentTimeMillis(),
                gameTime);
    }

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
            var ownerRef = tamable.getOwnerReference();
            if (ownerRef != null && !ownerRef.matches(player)) return false; // don't rely on `tamable.isOwnedBy(player)`!
        }

        if (entity instanceof AbstractHorse horse)
        {
            if (horse.isTamed() && horse.getOwnerReference() != null && !horse.getOwnerReference().matches(player)) return false;
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
