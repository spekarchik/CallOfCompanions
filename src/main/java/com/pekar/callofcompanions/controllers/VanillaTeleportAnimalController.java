package com.pekar.callofcompanions.controllers;

import com.pekar.callofcompanions.scheduler.CompanionEntryScheduler;
import com.pekar.callofcompanions.scheduler.CompanionEntryTask;
import net.minecraft.core.BlockPos;

class VanillaTeleportAnimalController extends SummonLoadedAnimalController
{
    protected VanillaTeleportAnimalController(SummonAnimalContext context)
    {
        super(context);
    }

    @Override
    public void run(BlockPos teleportPos)
    {
        int delay = level.getRandom().nextIntBetweenInclusive(30, 100);
        var task = new CompanionEntryTask(
                delay,
                companionEntry,
                player,
                (ticks, entry) -> {
                    if (ticks % 20 == 0)
                        showCrystalIsActiveParticles(player);

                    if (ticks == 29)
                    {
                        showAnimalTeleportParticles(level, animal);
                    }
                    else if (ticks == 9)
                    {
                        orderToStand(animal);
                    }
                    return false;
                },
                entry -> {
                    System.out.println("  Vanilla teleport .");
                    setGoal(animal, player);
                    playTeleportSound(level, animal);
                    showAnimalTeleportParticles(level, animal);
                    updateCompanionPos(level, companionData, entry);
                },
                entry -> {
                    System.out.println("  Vanilla teleport cancelled.");
                    playAnimalNotRespondSound(level, teleportPos);
                    showAnimalNotRespondParticles(level, teleportPos);
                }
        );
        CompanionEntryScheduler.UPDATE_POS_TASKS.add(task);
        System.out.println("  Vanilla teleport for " + companionEntry.type());
    }
}
