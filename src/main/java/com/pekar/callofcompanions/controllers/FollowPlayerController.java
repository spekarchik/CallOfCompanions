package com.pekar.callofcompanions.controllers;

import com.mojang.logging.LogUtils;
import com.pekar.callofcompanions.scheduler.CompanionEntryScheduler;
import com.pekar.callofcompanions.scheduler.CompanionEntryTask;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
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
                    if (ticks % 5 == 0)
                    {
                        if (animal.getNavigation().isDone())
                            setGoal(animal, player);
                        return animal.distanceToSqr(player) < 9;
                    }
                    return false;
                },
                entry -> {
                    LOGGER.debug("Follow-player task completed: companionType={}, companionId={}", entry.type(), entry.uuid());
                    if (animal.distanceToSqr(player) > 10 * 10)
                    {
                        var teleported = tryTeleportAnimalTo(level, animal.getUUID(), teleportPos, false);
                        if (teleported)
                        {
                            setGoal(animal, player);
                            playTeleportSound(level, animal);
                            showAnimalTeleportParticles(level, animal);
                        }
                        else
                        {
                            var name = CallCrystalHelper.buildAnimalName(entry.type(), entry.name());
                            player.sendSystemMessage(Component.translatable("message.callofcompanions.cant_teleport", name), true);
                            LOGGER.debug("Far teleport failed: companion couldn't find a safe place to teleport, companionType={}, companionId={}", entry.type(), entry.uuid());
                        }
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
