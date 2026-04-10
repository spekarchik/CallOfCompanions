package com.pekar.callofcompanions.controllers;

import com.pekar.callofcompanions.scheduler.CompanionEntryScheduler;
import com.pekar.callofcompanions.scheduler.CompanionEntryTask;
import net.minecraft.core.BlockPos;

class VanillaTeleportController extends LoadedAnimalSummonController
{
    protected VanillaTeleportController(SummonAnimalContext context)
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
                    if (ticks == 29)
                    {
                        showAnimalTeleportParticles(level, animal);
                    }
                    else if (ticks == 9)
                    {
                        System.out.println("  Ordered to stand: " + entry.type());
                        orderToStand(animal);
                    }
                    return false;
                },
                entry -> {
                    System.out.println("  Vanilla teleport .");
                    if (animal.distanceToSqr(player) > 12 * 12)
                    {
                        tryTeleportAnimalTo(level, animal.getUUID(), teleportPos);
                    }
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
