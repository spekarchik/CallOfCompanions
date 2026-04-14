package com.pekar.callofcompanions.controllers;

import com.mojang.logging.LogUtils;
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
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.nautilus.AbstractNautilus;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import org.slf4j.Logger;

import java.util.UUID;

public abstract class AnimalSummonController
{
    private static final Logger LOGGER = LogUtils.getLogger();

    protected final ServerPlayer player;
    protected final ServerLevel level;
    protected final CompanionData companionData;
    protected final CompanionEntry companionEntry;
    protected final ItemStack callCrystalStack;
    protected final float callDelayFactor;

    protected AnimalSummonController(SummonAnimalContext context)
    {
        this.player = context.player();
        this.level = context.level();
        this.companionData = context.companionData();
        this.companionEntry = context.companionEntry();
        this.callCrystalStack = context.callCrystalStack();
        this.callDelayFactor = context.callDelayFactor();
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

    protected boolean tryTeleportAnimalTo(Level level, UUID uuid, BlockPos pos)
    {
        var entity = level.getEntity(uuid);
        LOGGER.debug("Teleport attempt started: entityId={}, loaded={}", uuid, entity != null);
        if (entity instanceof Animal animal)
        {
            orderToStand(animal);
            var randomPos = getRandomPos(pos.above(), animal);
            if (randomPos == null) return false;
            animal.teleportTo(randomPos.getX() + 0.5, randomPos.getY(), randomPos.getZ() + 0.5);
            return true;
        }

        return false;
    }

    private BlockPos getRandomPos(BlockPos pos, Animal animal)
    {
        boolean isWaterAnimal = animal instanceof AbstractNautilus;

        final int delta = 3;

        // First try a number of random samples around the requested position
        for (int i = 0; i < 10; i++)
        {
            var newPos = randomAroundPos(pos, delta);

            if (CallCrystalHelper.isSafeForTeleleporting(level, newPos, isWaterAnimal))
            {
                return newPos;
            }
        }

        // If random sampling failed, iterate all positions in the square radius and return the first safe one
        for (int dy = isWaterAnimal ? -2 : 0; dy <= 0; dy++)
        {
            for (int dx = -delta; dx <= delta; dx++)
            {
                for (int dz = -delta; dz <= delta; dz++)
                {
                    //if (dx == 0 && dz == 0) continue;

                    var checkPos = pos.offset(dx, dy, dz);

                    if (CallCrystalHelper.isSafeForTeleleporting(level, checkPos, isWaterAnimal))
                    {
                        return checkPos;
                    }
                }
            }
        }

        // No safe position found — return original
        return null;
    }

    private BlockPos randomAroundPos(BlockPos pos, int delta)
    {
        var randomSource = player.getRandom();
        var dx = randomSource.nextIntBetweenInclusive(-delta, delta);
        var dz = randomSource.nextIntBetweenInclusive(-delta, delta);
        return pos.offset(dx, 0, dz);
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

    protected int applyDelayFactor(int initialDelay)
    {
        return (int)(initialDelay * callDelayFactor);
    }
}
