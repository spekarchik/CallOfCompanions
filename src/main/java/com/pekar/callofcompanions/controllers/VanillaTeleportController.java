package com.pekar.callofcompanions.controllers;

import com.mojang.logging.LogUtils;
import com.pekar.callofcompanions.data.CompanionEntry;
import com.pekar.callofcompanions.scheduler.CompanionEntryScheduler;
import com.pekar.callofcompanions.scheduler.CompanionEntryTask;
import com.pekar.callofcompanions.utils.Players;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;

class VanillaTeleportController extends LoadedAnimalSummonController
{
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int MAX_ANIMAL_DISTANCE_TO_AVOID_TELEPORTING = 12;

    protected VanillaTeleportController(SummonAnimalContext context)
    {
        super(context);
    }

    @Override
    public void run(BlockPos teleportPos)
    {
        int delay = playerLevel.getRandom().nextIntBetweenInclusive(applyDelayFactor(10), applyDelayFactor(100));
        var task = new CompanionEntryTask(
                delay,
                companionEntry,
                player,
                (ticks, entry) -> {
                    if (ticks == 29)
                    {
                        showAnimalTeleportParticles(playerLevel, animal);
                    }
                    else if (ticks == 9)
                    {
                        LOGGER.debug("Vanilla teleport pre-step: order companion to stand, companionType={}, companionId={}", entry.type(), entry.uuid());
                        orderToStand(animal);
                    }
                    return false;
                },
                entry -> {
                    moveAnimalTo(teleportPos, entry);
                },
                entry -> {
                    LOGGER.debug("Vanilla teleport cancelled: companionType={}, companionId={}", companionEntry.type(), companionEntry.uuid());
                    playAnimalNotRespondSound(playerLevel, teleportPos.below());
                    showAnimalNotRespondParticles(playerLevel, teleportPos.below());
                }
        );
        CompanionEntryScheduler.UPDATE_POS_TASKS.add(task);
        LOGGER.debug("Vanilla teleport scheduled: companionType={}, companionId={}, delayTicks={}", companionEntry.type(), companionEntry.uuid(), delay);
    }

    private void moveAnimalTo(BlockPos teleportPos, CompanionEntry entry)
    {
        LOGGER.debug("Vanilla teleport completing: companionType={}, companionId={}", entry.type(), entry.uuid());
        if (animal.distanceToSqr(player) > MAX_ANIMAL_DISTANCE_TO_AVOID_TELEPORTING * MAX_ANIMAL_DISTANCE_TO_AVOID_TELEPORTING)
        {
            var teleported = tryTeleportAnimalTo(playerLevel, animal.getUUID(), teleportPos, entry.dimension());
            if (teleported)
            {
                playTeleportSound(playerLevel, animal);
                showAnimalTeleportParticles(playerLevel, animal);

                if (teleportListener != null)
                    teleportListener.onTeleport(TeleportType.VANILLA_TELEPORT);
            }
            else
            {
                var name = CallCrystalHelper.buildAnimalName(entry.type(), entry.name());
                Players.sendOverlayMessage(player, Component.translatable("message.callofcompanions.cant_teleport", name));
                LOGGER.debug("Vanilla teleport failed: companion couldn't find a safe place to teleport, companionType={}, companionId={}", entry.type(), entry.uuid());
            }
        }
        setGoal(animal, player);
        CallCrystalHelper.updateCompanionPos(playerLevel, companionData, entry);
    }
}
