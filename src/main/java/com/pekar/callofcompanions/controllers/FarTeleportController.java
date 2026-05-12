package com.pekar.callofcompanions.controllers;

import com.mojang.logging.LogUtils;
import com.pekar.callofcompanions.Config;
import com.pekar.callofcompanions.data.CompanionEntry;
import com.pekar.callofcompanions.scheduler.CompanionEntryScheduler;
import com.pekar.callofcompanions.scheduler.CompanionEntryTask;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import org.slf4j.Logger;

import java.util.UUID;

class FarTeleportController extends AnimalSummonController
{
    private static final Logger LOGGER = LogUtils.getLogger();

    protected FarTeleportController(SummonAnimalContext context)
    {
        super(context);
    }

    @Override
    public void run(BlockPos teleportPos)
    {
        int postponeTicks = getPostponeTicks();

        var delayTask = new CompanionEntryTask(
                postponeTicks,
                companionEntry,
                player,
                null,
                entry -> {
                    LOGGER.debug("Far teleport delay completed: companionType={}, companionId={}", entry.type(), entry.uuid());
                    createTeleportTask(teleportPos, entry);
                },
                entry -> {
                    showAnimalNotRespondParticles(playerLevel, teleportPos);
                    playAnimalNotRespondSound(playerLevel, teleportPos);
                    LOGGER.debug("Far teleport delay cancelled: companionType={}, companionId={}", companionEntry.type(), companionEntry.uuid());
                });

        CompanionEntryScheduler.DELAY_TASKS.add(delayTask);
        LOGGER.debug("Far teleport delay scheduled: companionType={}, companionId={}, delayTicks={}", companionEntry.type(), companionEntry.uuid(), postponeTicks);
    }

    private int getPostponeTicks()
    {
        int postponeTicks = playerLevel.getRandom().nextIntBetweenInclusive(applyDelayFactor(5), applyDelayFactor(100));

        if (companionEntry != null && !playerLevel.dimension().equals(companionEntry.dimension()) && allowCrossDimensionalTeleports)
        {
            double multiplier = Config.DEEP_CRYSTAL_CROSS_DIMENSION_DELAY_MULTIPLIER.get();
            int adjusted = Math.max(1, (int)Math.round(postponeTicks * multiplier));
            LOGGER.debug("Applying cross-dimension delay multiplier: original={}, multiplier={}, adjusted={}", postponeTicks, multiplier, adjusted);
            postponeTicks = adjusted;
        }
        return postponeTicks;
    }

    private void createTeleportTask(BlockPos teleportPos, CompanionEntry companionEntry)
    {
        final int LOAD_CHUNK_RADIUS = Config.FAR_TELEPORT_CHUNK_RADIUS.getAsInt();
        var animalDimension = companionEntry.dimension();
        boolean isCrossDimensionalTeleport = !playerLevel.dimension().equals(animalDimension);

        if (!allowCrossDimensionalTeleports && isCrossDimensionalTeleport)
        {
            playAnimalNotRespondSound(playerLevel, teleportPos.below());
            showAnimalNotRespondParticles(playerLevel, teleportPos.below());
            var name = CallCrystalHelper.buildAnimalName(companionEntry.type(), companionEntry.name());
            player.sendSystemMessage(Component.translatable("message.callofcompanions.wrong_dimension", name));
            LOGGER.debug("Far teleport cancelled: wrong dimension, companionType={}, companionId={}, companionPos={}, companionDimension={}", companionEntry.type(), companionEntry.uuid(), companionEntry.pos(), companionEntry.dimension());
            return;
        }

        var animalLevel = playerLevel.getServer().getLevel(animalDimension);
        if (animalLevel == null)
        {
            handleIllegalState(companionEntry);
            return;
        }

        // Ensure the companion's chunk and neighbor chunks are loaded within the configured radius
        int centerSectionX = SectionPos.blockToSectionCoord(companionEntry.pos().getX());
        int centerSectionZ = SectionPos.blockToSectionCoord(companionEntry.pos().getZ());

        for (int dx = -LOAD_CHUNK_RADIUS; dx <= LOAD_CHUNK_RADIUS; dx++)
        {
            for (int dz = -LOAD_CHUNK_RADIUS; dz <= LOAD_CHUNK_RADIUS; dz++)
            {
                animalLevel.getChunkSource().getChunk(centerSectionX + dx, centerSectionZ + dz, ChunkStatus.FULL, true);
            }
        }

        var task = new CompanionEntryTask(
                Config.FAR_TELEPORT_WAIT_TICKS.getAsInt(),
                companionEntry,
                player,
                (ticks, entry) -> {
                    if (ticks % 40 == 1)
                    {
                        showParticles(playerLevel, teleportPos, ParticleTypes.PORTAL);
                    }
                    return checkEntityLoaded(playerLevel, entry.uuid());
                },
                entry ->
                {
                    teleportAnimalTo(teleportPos, entry, isCrossDimensionalTeleport);
                },
                entry ->
                {
                    playAnimalNotRespondSound(playerLevel, teleportPos.below());
                    showAnimalNotRespondParticles(playerLevel, teleportPos.below());
                    LOGGER.debug("Far teleport cancelled: companionType={}, companionId={}", companionEntry.type(), companionEntry.uuid());
                });

        CompanionEntryScheduler.TELEPORT_TASKS.add(task);
        LOGGER.debug("Far teleport task scheduled: companionType={}, companionId={}", companionEntry.type(), companionEntry.uuid());
    }

    private void teleportAnimalTo(BlockPos teleportPos, CompanionEntry entry, boolean isCrossDimensionalTeleport)
    {
        var entity = playerLevel.getEntity(entry.uuid());
        if (!CallCrystalHelper.canSummonAnimal(entity, player))
        {
            LOGGER.debug("Far teleport skipped: companion can't be summoned by player, companionType={}, companionId={}, companionPos={}, companionDimension={}, player={}", entry.type(), entry.uuid(), entry.pos(), entry.dimension(), player.getDisplayName());
            return;
        }

        var teleported = tryTeleportAnimalTo(playerLevel, entry.uuid(), teleportPos, entry.dimension());
        if (teleported)
        {
            if (playerLevel.getEntity(entry.uuid()) instanceof Animal animal)
            {
                showAnimalTeleportParticles(playerLevel, animal);
                playTeleportSound(playerLevel, animal);
                setGoal(animal, player);
            }

            if (teleportListener != null)
                teleportListener.onTeleport(isCrossDimensionalTeleport ? TeleportType.CROSS_DIMENSION_TELEPORT : TeleportType.FAR_TELEPORT);

            LOGGER.debug("Far teleport completed: companionType={}, companionId={}, companionPos={}, companionDimension={}", entry.type(), entry.uuid(), entry.pos(), entry.dimension());
        }
        else
        {
            playAnimalNotRespondSound(playerLevel, teleportPos.below());
            showAnimalNotRespondParticles(playerLevel, teleportPos.below());
            var name = CallCrystalHelper.buildAnimalName(entry.type(), entry.name());
            if (playerLevel.getEntity(entry.uuid()) == null)
            {
                player.sendSystemMessage(Component.translatable("message.callofcompanions.not_found", name));
                LOGGER.debug("Far teleport failed: companion not found, companionType={}, companionId={}, companionPos={}, companionDimension={}", entry.type(), entry.uuid(), entry.pos(), entry.dimension());
            }
            else
            {
                player.sendSystemMessage(Component.translatable("message.callofcompanions.cant_teleport", name), true);
                LOGGER.debug("Far teleport failed: companion couldn't find a safe place to teleport, companionType={}, companionId={}, companionPos={}, companionDimension={}", entry.type(), entry.uuid(), entry.pos(), entry.dimension());
            }
        }

        CallCrystalHelper.updateCompanionPos(playerLevel, companionData, entry);
    }

    private boolean checkEntityLoaded(ServerLevel level, UUID uuid)
    {
        var entity = level.getEntity(uuid);
        return entity != null;
    }

    private static void handleIllegalState(CompanionEntry companionEntry)
    {
        var msg = String.format("Could not get server level for animal dimension %s (companionType=%s, companionId=%s)",
                companionEntry.dimension(), companionEntry.type(), companionEntry.uuid());
        LOGGER.error(msg);
        // This is an unexpected state: the server doesn't expose a level for the companion's dimension.
        // Throw an unchecked exception so the caller can notice and the situation can be diagnosed by crash reports/logs.
        throw new IllegalStateException(msg);
    }
}
