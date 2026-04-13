package com.pekar.callofcompanions.controllers;

import com.mojang.logging.LogUtils;
import com.pekar.callofcompanions.scheduler.CompanionEntryScheduler;
import com.pekar.callofcompanions.scheduler.CompanionEntryTask;
import net.minecraft.core.BlockPos;
import org.slf4j.Logger;

class FollowPlayerController extends LoadedAnimalSummonController
{
    private static final Logger LOGGER = LogUtils.getLogger();

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
                (ticks, entry) -> {
                    if (ticks % 20 == 0)
                    {
                        setGoal(animal, player);
                        return animal.distanceToSqr(player) < 9;
                    }
                    return false;
                },
                entry -> {
                    LOGGER.debug("Follow-player task completed: companionType={}, companionId={}", entry.type(), entry.uuid());
                    if (animal.distanceToSqr(player) > 10 * 10)
                    {
                        tryTeleportAnimalTo(level, animal.getUUID(), teleportPos, false);
                        setGoal(animal, player);
                        playTeleportSound(level, animal);
                        showAnimalTeleportParticles(level, animal);
                    }
                    CallCrystalHelper.updateCompanionPos(level, companionData, entry);
                },
                entry -> {
                    LOGGER.debug("Follow-player task cancelled: companionType={}, companionId={}", companionEntry.type(), companionEntry.uuid());
                }
        );
        CompanionEntryScheduler.UPDATE_POS_TASKS.add(task);
        LOGGER.debug("Follow-player task scheduled: companionType={}, companionId={}, timeoutTicks={}", companionEntry.type(), companionEntry.uuid(), 300);
    }
}
