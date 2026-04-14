package com.pekar.callofcompanions.controllers;

import com.mojang.logging.LogUtils;
import com.pekar.callofcompanions.scheduler.CompanionEntryScheduler;
import com.pekar.callofcompanions.scheduler.CompanionEntryTask;
import net.minecraft.core.BlockPos;
import org.slf4j.Logger;

class NearTeleportController extends LoadedAnimalSummonController
{
    private static final Logger LOGGER = LogUtils.getLogger();

    protected NearTeleportController(SummonAnimalContext context)
    {
        super(context);
    }

    @Override
    public void run(BlockPos teleportPos)
    {
        orderToStand(animal);
        showAnimalTeleportParticles(level, animal);
        int delay = level.getRandom().nextIntBetweenInclusive(applyDelayFactor(10), applyDelayFactor(100));
        var task = new CompanionEntryTask(
                delay,
                companionEntry,
                player,
                (ticks, entry) -> {
                    if (ticks % 10 == 0)
                    {
                        showAnimalTeleportParticles(level, animal);
                    }
                    return false;
                },
                entry -> {
                    boolean teleported = tryTeleportAnimalTo(level, entry.uuid(), teleportPos);
                    if (teleported)
                    {
                        playTeleportSound(level, animal);
                        showAnimalTeleportParticles(level, animal);
                        setGoal(animal, player);
                        CallCrystalHelper.updateCompanionPos(level, companionData, companionEntry);
                        LOGGER.debug("Near teleport completed: companionType={}, companionId={}", entry.type(), entry.uuid());
                    }
                    else
                    {
                        playAnimalNotRespondSound(level, teleportPos);
                        showAnimalNotRespondParticles(level, teleportPos);
                        LOGGER.debug("Near teleport failed: companion not found, companionType={}, companionId={}", entry.type(), entry.uuid());
                    }
                },
                _ -> {
                    LOGGER.debug("Near teleport cancelled: companionType={}, companionId={}", companionEntry.type(), companionEntry.uuid());
                    playAnimalNotRespondSound(level, teleportPos);
                    showAnimalNotRespondParticles(level, teleportPos);
                }
        );
        CompanionEntryScheduler.TELEPORT_TASKS.add(task);
        LOGGER.debug("Near teleport scheduled: companionType={}, companionId={}, delayTicks={}", companionEntry.type(), companionEntry.uuid(), delay);
    }
}
