package com.pekar.callofcompanions.controllers;

import com.pekar.callofcompanions.scheduler.CompanionEntryScheduler;
import com.pekar.callofcompanions.scheduler.CompanionEntryTask;
import net.minecraft.core.BlockPos;

class GoalToPlayerAnimalController extends SummonLoadedAnimalController
{
    protected GoalToPlayerAnimalController(SummonAnimalContext context)
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
                (ticks, _) -> {
                    if (ticks % 20 == 0)
                    {
                        showCrystalIsActiveParticles(player);
                        setGoal(animal, player);
                        return animal.distanceToSqr(player) < 9;
                    }
                    return false;
                },
                entry -> {
                    showAnimalTeleportParticles(level, animal);
                    updateCompanionPos(level, companionData, entry);
                    saveStackChanges(player, callCrystalStack, companionData);
                },
                null
        );
        CompanionEntryScheduler.UPDATE_POS_TASKS.add(task);
        System.out.println("  Already at goal for " + companionEntry.type());
    }
}
