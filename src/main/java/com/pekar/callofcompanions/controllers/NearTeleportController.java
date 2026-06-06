package com.pekar.callofcompanions.controllers;

import com.mojang.logging.LogUtils;
import com.pekar.callofcompanions.data.CompanionEntry;
import com.pekar.callofcompanions.scheduler.CompanionEntryScheduler;
import com.pekar.callofcompanions.scheduler.CompanionEntryTask;
import com.pekar.callofcompanions.utils.Players;
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
        showAnimalTeleportParticles(playerLevel, animal);
        int delay = playerLevel.getRandom().nextIntBetweenInclusive(applyDelayFactor(10), applyDelayFactor(100));
        var task = new CompanionEntryTask(
                delay,
                companionEntry,
                player,
                (ticks, entry) -> {
                    if (ticks % 10 == 0)
                    {
                        showAnimalTeleportParticles(playerLevel, animal);
                    }
                    return false;
                },
                entry -> {
                    moveAnimalTo(teleportPos, entry);
                },
                _ -> {
                    LOGGER.debug("Near teleport cancelled: companionType={}, companionId={}", companionEntry.type(), companionEntry.uuid());
                    playAnimalNotRespondSound(playerLevel, teleportPos.below());
                    showAnimalNotRespondParticles(playerLevel, teleportPos.below());
                }
        );
        CompanionEntryScheduler.TELEPORT_TASKS.add(task);
        LOGGER.debug("Near teleport scheduled: companionType={}, companionId={}, delayTicks={}", companionEntry.type(), companionEntry.uuid(), delay);
    }

    private void moveAnimalTo(BlockPos teleportPos, CompanionEntry entry)
    {
        boolean teleported = tryTeleportAnimalTo(playerLevel, entry.uuid(), teleportPos, entry.dimension());
        if (teleported)
        {
            playTeleportSound(playerLevel, animal);
            showAnimalTeleportParticles(playerLevel, animal);
            setGoal(animal, player);

            if (teleportListener != null)
                teleportListener.onTeleport(TeleportType.NEAR_TELEPORT);

            LOGGER.debug("Near teleport completed: companionType={}, companionId={}", entry.type(), entry.uuid());
        }
        else
        {
            playAnimalNotRespondSound(playerLevel, teleportPos.below());
            showAnimalNotRespondParticles(playerLevel, teleportPos.below());
            var name = CallCrystalHelper.buildAnimalName(entry.type(), entry.name());
            Players.sendOverlayMessage(player, Component.translatable("message.callofcompanions.cant_teleport", name));
            LOGGER.debug("Near teleport failed: companion couldn't find a safe place to teleport, companionType={}, companionId={}", entry.type(), entry.uuid());
        }

        CallCrystalHelper.updateCompanionPos(playerLevel, companionData, companionEntry);
    }
}
