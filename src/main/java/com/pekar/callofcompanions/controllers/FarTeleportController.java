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
import net.minecraft.server.level.TicketType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
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
        int postponeTicks = level.getRandom().nextIntBetweenInclusive(applyDelayFactor(5), applyDelayFactor(100));
        var delayTask = new CompanionEntryTask(
                postponeTicks,
                companionEntry,
                player,
                null,
                entry -> {
                    LOGGER.debug("Far teleport delay completed: companionType={}, companionId={}", entry.type(), entry.uuid());
                    createTeleportTask(teleportPos, entry);
                },
                _ -> {
                    showAnimalNotRespondParticles(level, teleportPos);
                    playAnimalNotRespondSound(level, teleportPos);
                    LOGGER.debug("Far teleport delay cancelled: companionType={}, companionId={}", companionEntry.type(), companionEntry.uuid());
                });

        CompanionEntryScheduler.DELAY_TASKS.add(delayTask);
        LOGGER.debug("Far teleport delay scheduled: companionType={}, companionId={}, delayTicks={}", companionEntry.type(), companionEntry.uuid(), postponeTicks);
    }

    private void createTeleportTask(BlockPos teleportPos, CompanionEntry companionEntry)
    {
        final int LOAD_CHUNK_RADIUS = Config.FAR_TELEPORT_CHUNK_RADIUS.getAsInt();

        if (!level.dimension().equals(companionEntry.dimension()))
        {
            playAnimalNotRespondSound(level, teleportPos);
            showAnimalNotRespondParticles(level, teleportPos);
            var name = buildAnimalName(companionEntry.type(), companionEntry.name());
            player.sendSystemMessage(Component.translatable("message.callofcompanions.wrong_dimension", name));
            LOGGER.debug("Far teleport cancelled: wrong dimension, companionType={}, companionId={}, companionDimension={}", companionEntry.type(), companionEntry.uuid(), companionEntry.dimension());
            return;
        }

        var chunkPos = new ChunkPos(SectionPos.blockToSectionCoord(companionEntry.pos().getX()), SectionPos.blockToSectionCoord(companionEntry.pos().getZ()));
        level.getChunkSource().addTicketWithRadius(TicketType.PORTAL, chunkPos, LOAD_CHUNK_RADIUS);

        var task = new CompanionEntryTask(
                100,
                companionEntry,
                player,
                (ticks, entry) -> {
                    if (ticks % 40 == 1)
                    {
                        showParticles(level, teleportPos, ParticleTypes.PORTAL);
                    }
                    return checkEntityLoaded(level, entry.uuid());
                },
                entry ->
                {
                    var entity = level.getEntity(entry.uuid());
                    if (!canSummonAnimal(entity, player))
                    {
                        LOGGER.debug("Far teleport skipped: companion can't be summoned by player, companionType={}, companionId={}, player={}", entry.type(), entry.uuid(), player.getDisplayName());
                        return;
                    }

                    var result = tryTeleportAnimalTo(level, entry.uuid(), teleportPos);
                    if (result)
                    {
                        showParticles(level, teleportPos, ParticleTypes.PORTAL);
                        if (level.getEntity(entry.uuid()) instanceof Animal animal)
                            playTeleportSound(level, animal);
                        LOGGER.debug("Far teleport completed: companionType={}, companionId={}", entry.type(), entry.uuid());
                    }
                    else
                    {
                        playAnimalNotRespondSound(level, teleportPos);
                        showAnimalNotRespondParticles(level, teleportPos);
                        var name = buildAnimalName(entry.type(), entry.name());
                        player.sendSystemMessage(Component.translatable("message.callofcompanions.not_found", name));
                        LOGGER.debug("Far teleport failed: companion not found, companionType={}, companionId={}", entry.type(), entry.uuid());
                    }

                    updateCompanionPos(level, companionData, entry);
                    level.getChunkSource().removeTicketWithRadius(TicketType.PORTAL, chunkPos, LOAD_CHUNK_RADIUS);
                },
                _ ->
                {
                    level.getChunkSource().removeTicketWithRadius(TicketType.PORTAL, chunkPos, LOAD_CHUNK_RADIUS);
                    playAnimalNotRespondSound(level, teleportPos);
                    showAnimalNotRespondParticles(level, teleportPos);
                    LOGGER.debug("Far teleport cancelled: companionType={}, companionId={}", companionEntry.type(), companionEntry.uuid());
                });

        CompanionEntryScheduler.TELEPORT_TASKS.add(task);
        LOGGER.debug("Far teleport task scheduled: companionType={}, companionId={}", companionEntry.type(), companionEntry.uuid());
    }

    private boolean checkEntityLoaded(Level level, UUID uuid)
    {
        var entity = level.getEntity(uuid);
        return entity != null;
    }
}
