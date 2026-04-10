package com.pekar.callofcompanions.controllers;

import com.pekar.callofcompanions.data.CompanionData;
import com.pekar.callofcompanions.data.CompanionEntry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.UUID;

public abstract class AnimalSummonController
{
    protected final ServerPlayer player;
    protected final ServerLevel level;
    protected final CompanionData companionData;
    protected final CompanionEntry companionEntry;
    protected final ItemStack callCrystalStack;

    protected AnimalSummonController(SummonAnimalContext context)
    {
        this.player = context.player();
        this.level = context.level();
        this.companionData = context.companionData();
        this.companionEntry = context.companionEntry();
        this.callCrystalStack = context.callCrystalStack();
    }

    protected void setGoal(Animal animal, Player player)
    {
        animal.getNavigation().moveTo(player, 1.4);
    }

    public abstract void run(BlockPos teleportPos);

    protected void showParticles(ServerLevel serverLevel, BlockPos clickPos, SimpleParticleType particleType)
    {
        serverLevel.sendParticles(
                particleType,
                clickPos.getX(), clickPos.getY(), clickPos.getZ(),
                100,
                0.5, 0.5, 0.5,
                0.1
        );
    }

    protected void showAnimalTeleportParticles(ServerLevel serverLevel, Animal animal)
    {
        var pos = animal.blockPosition();
        serverLevel.sendParticles(
                ParticleTypes.PORTAL,
                pos.getX(), pos.getY(), pos.getZ(),
                100,
                0.5, 0.5, 0.5,
                0.1
        );
    }

    protected void showAnimalNotRespondParticles(ServerLevel serverLevel, BlockPos pos)
    {
        serverLevel.sendParticles(
                ParticleTypes.SMOKE,
                pos.getX(), pos.getY(), pos.getZ(),
                100,
                0.5, 0.5, 0.5,
                0.1
        );
    }

    public static void updateCompanionPos(ServerLevel level, CompanionData companions, CompanionEntry companion)
    {
        var entity = level.getEntity(companion.uuid());
        if (entity == null)
        {
            companions.add(companion.getAsLost());
            return;
        }

        var newEntry = companion.getWith(entity.blockPosition());
        companions.add(newEntry);
    }

    public static String buildAnimalName(String animalType, String animalName)
    {
        return animalName.equals(animalType) ? animalType : animalType + " '" + animalName + "'";
    }

    protected boolean tryTeleportAnimalTo(Level level, UUID uuid, BlockPos pos)
    {
        var entity = level.getEntity(uuid);
        System.out.println("  Trying to teleport. Is null: " + (entity == null));
        if (entity instanceof Animal animal)
        {
            orderToStand(animal);
            BlockPos randomPos = getRandomPos(pos.above());
            animal.teleportTo(randomPos.getX() + 0.5, randomPos.getY(), randomPos.getZ() + 0.5);
            return true;
        }

        return false;
    }

    private BlockPos getRandomPos(BlockPos pos)
    {
        for (int i = 0; i < 10; i++)
        {
            var newPos = randomAroundPos(pos, 3);

            var ground = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, newPos);

            if (Math.abs(ground.getY() - pos.getY()) <= 3 && isSafe(level, ground))
            {
                return ground;
            }
        }

        return pos;
    }

    private BlockPos randomAroundPos(BlockPos pos, int delta)
    {
        var randomSource = player.getRandom();
        var dx = randomSource.nextIntBetweenInclusive(-delta, delta);
        var dz = randomSource.nextIntBetweenInclusive(-delta, delta);
        return pos.offset(dx, 0, dz);
    }

    public static boolean isSafe(Level level, BlockPos pos)
    {
        var below = level.getBlockState(pos.below());
        var at = level.getBlockState(pos);
        var above = level.getBlockState(pos.above());
        var above2 = level.getBlockState(pos.above(2));

        return below.isSolidRender() &&
                        !below.is(BlockTags.FIRE) &&
                        !below.is(Blocks.LAVA) &&
                        at.isAir() &&                    // body
                        above.isAir() && above2.isAir(); // head
    }

    protected void orderToStand(Animal animal)
    {
        if (animal instanceof TamableAnimal tamable)
        {
            if (tamable.isInSittingPose())
            {
                tamable.setOrderedToSit(false);
                tamable.setInSittingPose(false);
            }
        }
    }

    private void playSound(ServerLevel level, BlockPos pos, SoundEvent sound)
    {
        level.playSound(null, pos, sound, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    protected void playTeleportSound(ServerLevel level, Animal animal)
    {
        level.playSound(null, animal.blockPosition(), SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 0.5F, 0.2F);
    }

    protected void playAnimalNotRespondSound(ServerLevel level, BlockPos pos)
    {
        level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.5F, 1.0F);
    }
}
