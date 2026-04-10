package com.pekar.callofcompanions.controllers;

import com.pekar.callofcompanions.scheduler.CompanionEntryScheduler;
import com.pekar.callofcompanions.scheduler.CompanionEntryTask;
import net.minecraft.core.BlockPos;

class FollowPlayerController extends LoadedAnimalSummonController
{
    protected FollowPlayerController(SummonAnimalContext context)
    {
        super(context);
    }

    @Override
    public void run(BlockPos teleportPos)
    {
        orderToStand(animal);
        setGoal(animal, player);
        var task = new CompanionEntryTask(
                300,
                companionEntry,
                player,
                (ticks, _) -> {
                    if (ticks % 20 == 0)
                    {
                        setGoal(animal, player);
                        return animal.distanceToSqr(player) < 9;
                    }
                    return false;
                },
                entry -> {
                    System.out.println("  Goal reached.");
                    showAnimalTeleportParticles(level, animal);
                    updateCompanionPos(level, companionData, entry);
                },
                _ -> {
                    System.out.println("  Goal cancelled.");
                }
        );
        CompanionEntryScheduler.UPDATE_POS_TASKS.add(task);
        System.out.println("  Goal for " + companionEntry.type());
    }
}
