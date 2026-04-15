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
import net.minecraft.world.entity.animal.horse.AbstractHorse;
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
