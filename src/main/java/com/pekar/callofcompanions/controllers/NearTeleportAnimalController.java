package com.pekar.callofcompanions.controllers;

import com.pekar.callofcompanions.scheduler.CompanionEntryScheduler;
import com.pekar.callofcompanions.scheduler.CompanionEntryTask;
import net.minecraft.core.BlockPos;

class NearTeleportAnimalController extends SummonLoadedAnimalController
{
    protected NearTeleportAnimalController(SummonAnimalContext context)
    {
        super(context);
    }

    @Override
    public void run(BlockPos teleportPos)
    {
        orderToStand(animal);
        showAnimalTeleportParticles(level, animal);
        int delay = level.getRandom().nextIntBetweenInclusive(20, 100);
        var task = new CompanionEntryTask(
                delay,
                companionEntry,
                (ticks, entry) -> {
                    if (ticks % 20 == 0)
                    {
                        showCrystalIsActiveParticles(player);
                        showAnimalTeleportParticles(level, animal);
                    }
                    return false;
                },
                entry -> {
                    boolean teleported = tryTeleportAnimalTo(level, entry.uuid(), teleportPos);
                    System.out.println("  Trying near teleporting...");
                    if (teleported)
                    {
                        playTeleportSound(level, animal);
                        showAnimalTeleportParticles(level, animal);
                        updateCompanionPos(level, companionData, companionEntry);
                        saveStackChanges(player, callCrystalStack, companionData);
                    }
                    else
                    {
                        playAnimalNotRespondSound(level, teleportPos);
                        showAnimalNotRespondParticles(level, teleportPos);
                    }
                },
                _ -> {
                    System.out.println("  Near teleporting cancelled.");
                    playAnimalNotRespondSound(level, teleportPos);
                    showAnimalNotRespondParticles(level, teleportPos);
                }
        );
        CompanionEntryScheduler.UPDATE_POS_TASKS.add(task);
        System.out.println("  Near teleporting for " + companionEntry.type());
    }
}
