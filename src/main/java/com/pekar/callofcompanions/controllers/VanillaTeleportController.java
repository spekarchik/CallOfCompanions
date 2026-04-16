package com.pekar.callofcompanions.controllers;

import com.mojang.logging.LogUtils;
import com.pekar.callofcompanions.scheduler.CompanionEntryScheduler;
import com.pekar.callofcompanions.scheduler.CompanionEntryTask;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;

class VanillaTeleportController extends LoadedAnimalSummonController
{
    private static final Logger LOGGER = LogUtils.getLogger();

    protected VanillaTeleportController(SummonAnimalContext context)
    {
        super(context);
    }

    @Override
    public void run(BlockPos teleportPos)
    {
        int delay = level.getRandom().nextIntBetweenInclusive(applyDelayFactor(10), applyDelayFactor(100));
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
                        LOGGER.debug("Vanilla teleport pre-step: order companion to stand, companionType={}, companionId={}", entry.type(), entry.uuid());
                        orderToStand(animal);
                    }
                    return false;
                },
                entry -> {
                    LOGGER.debug("Vanilla teleport completing: companionType={}, companionId={}", entry.type(), entry.uuid());
                    if (animal.distanceToSqr(player) > 12 * 12)
                    {
                        var teleported = tryTeleportAnimalTo(level, animal.getUUID(), teleportPos);
                        if (teleported)
                        {
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
                    setGoal(animal, player);
                    CallCrystalHelper.updateCompanionPos(level, companionData, entry);
                },
                entry -> {
                    LOGGER.debug("Vanilla teleport cancelled: companionType={}, companionId={}", companionEntry.type(), companionEntry.uuid());
                    playAnimalNotRespondSound(level, teleportPos.below());
                    showAnimalNotRespondParticles(level, teleportPos.below());
                }
        );
        CompanionEntryScheduler.UPDATE_POS_TASKS.add(task);
        LOGGER.debug("Vanilla teleport scheduled: companionType={}, companionId={}, delayTicks={}", companionEntry.type(), companionEntry.uuid(), delay);
    }
}
