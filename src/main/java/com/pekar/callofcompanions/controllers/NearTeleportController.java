package com.pekar.callofcompanions.controllers;

import com.mojang.logging.LogUtils;
import com.pekar.callofcompanions.scheduler.CompanionEntryScheduler;
import com.pekar.callofcompanions.scheduler.CompanionEntryTask;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
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
                        LOGGER.debug("Near teleport completed: companionType={}, companionId={}", entry.type(), entry.uuid());
                    }
                    else
                    {
                        playAnimalNotRespondSound(level, teleportPos);
                        showAnimalNotRespondParticles(level, teleportPos);
                        var name = CallCrystalHelper.buildAnimalName(entry.type(), entry.name());
                        player.sendSystemMessage(Component.translatable("message.callofcompanions.cant_teleport", name), true);
                        LOGGER.debug("Far teleport failed: companion couldn't find a safe place to teleport, companionType={}, companionId={}", entry.type(), entry.uuid());
                    }

                    CallCrystalHelper.updateCompanionPos(level, companionData, companionEntry);
                },
                entry -> {
                    LOGGER.debug("Near teleport cancelled: companionType={}, companionId={}", companionEntry.type(), companionEntry.uuid());
                    playAnimalNotRespondSound(level, teleportPos);
                    showAnimalNotRespondParticles(level, teleportPos);
                }
        );
        CompanionEntryScheduler.TELEPORT_TASKS.add(task);
        LOGGER.debug("Near teleport scheduled: companionType={}, companionId={}, delayTicks={}", companionEntry.type(), companionEntry.uuid(), delay);
    }
}
