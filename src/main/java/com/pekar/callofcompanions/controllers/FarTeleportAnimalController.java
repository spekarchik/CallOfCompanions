package com.pekar.callofcompanions.controllers;

import com.pekar.callofcompanions.data.CompanionEntry;
import com.pekar.callofcompanions.scheduler.CompanionEntryScheduler;
import com.pekar.callofcompanions.scheduler.CompanionEntryTask;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.UUID;

class FarTeleportAnimalController extends SummonAnimalController
{
    protected FarTeleportAnimalController(SummonAnimalContext context)
    {
        super(context);
    }

    @Override
    public void run(BlockPos teleportPos)
    {
        int postponeTicks = level.getRandom().nextIntBetweenInclusive(5, 100);
        var delayTask = new CompanionEntryTask(
                postponeTicks,
                companionEntry,
                player,
                (ticks, _) -> {
                    if (ticks % 20 == 0)
                        showCrystalIsActiveParticles(player);
                    return false;
                },
                entry -> {
                    System.out.println("  FarTeleport: teleporting...");
                    createTeleportTask(teleportPos, entry);
                },
                _ -> {
                    showAnimalNotRespondParticles(level, teleportPos);
                    playAnimalNotRespondSound(level, teleportPos);
                    System.out.println("  FarTeleport: Postpone cancelled.");
                });

        CompanionEntryScheduler.DELAY_TASKS.add(delayTask);
        System.out.println("  Far teleport for " + companionEntry.type());
    }

    private void createTeleportTask(BlockPos teleportPos, CompanionEntry companionEntry)
    {
        if (!level.dimension().equals(companionEntry.dimension())) return;

        var chunkPos = new ChunkPos(SectionPos.blockToSectionCoord(companionEntry.pos().getX()), SectionPos.blockToSectionCoord(companionEntry.pos().getZ()));
        level.getChunkSource().addTicketWithRadius(TicketType.PORTAL, chunkPos, 2);
//                    var ticket = new Ticket(TicketType.PORTAL, 2);
//                    level.getChunkSource().addTicket(ticket, chunkPos);

        var task = new CompanionEntryTask(
                100,
                companionEntry,
                player,
                (ticks, entry) -> {
                    if (ticks % 40 == 1)
                    {
                        showCrystalIsActiveParticles(player);
                        showParticles(level, teleportPos, ParticleTypes.PORTAL);
                    }
                    return checkEntityLoaded(level, entry.uuid());
                },
                entry ->
                {
                    var result = tryTeleportAnimalTo(level, entry.uuid(), teleportPos);
                    if (result)
                    {
                        showParticles(level, teleportPos, ParticleTypes.PORTAL);
                        if (level.getEntity(entry.uuid()) instanceof Animal animal)
                            playTeleportSound(level, animal);
                        System.out.println("  Far Teleported.");
                    }
                    else
                    {
                        playAnimalNotRespondSound(level, teleportPos);
                        showAnimalNotRespondParticles(level, teleportPos);
                        var name = buildAnimalName(entry.type(), entry.name());
                        player.sendSystemMessage(Component.translatable("message.callofcompanions.not_found", name));
                        System.out.println("  Far teleport: Not found.");
                    }

                    updateCompanionPos(level, companionData, entry);
                    level.getChunkSource().removeTicketWithRadius(TicketType.PORTAL, chunkPos, 2);
                },
                _ ->
                {
                    level.getChunkSource().removeTicketWithRadius(TicketType.PORTAL, chunkPos, 2);
                    playAnimalNotRespondSound(level, teleportPos);
                    showAnimalNotRespondParticles(level, teleportPos);
                    System.out.println("  Far Teleport cancelled.");
                });

        CompanionEntryScheduler.TELEPORT_TASKS.add(task);
    }

    private boolean checkEntityLoaded(Level level, UUID uuid)
    {
        var entity = level.getEntity(uuid);
        return entity != null;
    }
}
