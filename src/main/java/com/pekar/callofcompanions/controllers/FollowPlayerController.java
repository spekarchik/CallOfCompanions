package com.pekar.callofcompanions.controllers;

import com.mojang.logging.LogUtils;
import com.pekar.callofcompanions.entity.EntityRegistry;
import com.pekar.callofcompanions.data.CompanionEntry;
import com.pekar.callofcompanions.scheduler.CompanionEntryScheduler;
import com.pekar.callofcompanions.scheduler.CompanionEntryTask;
import com.pekar.callofcompanions.utils.Players;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;

import static com.pekar.callofcompanions.Config.PREVENT_PETS_INVISIBILITY_WORKAROUND_1_21_1;

class FollowPlayerController extends LoadedAnimalSummonController
{
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int MAX_ANIMAL_DISTANCE_TO_AVOID_TELEPORTING = 10;

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
                    moveAnimalTo(teleportPos, entry);
                },
                entry -> {
                    if (PREVENT_PETS_INVISIBILITY_WORKAROUND_1_21_1.isTrue()
                            && animal.getType().is(EntityRegistry.ANIMALS_CAN_TELEPORT_TO_PLAYER)
                            && animal.distanceToSqr(player) < 10 * 10)
                    {
                        recreateAnimal(playerLevel, animal, animal.getX(), animal.getY(), animal.getZ());
                    }
                    LOGGER.debug("Follow-player task cancelled: companionType={}, companionId={}", companionEntry.type(), companionEntry.uuid());
                }
        );
        CompanionEntryScheduler.UPDATE_POS_TASKS.add(task);
        LOGGER.debug("Follow-player task scheduled: companionType={}, companionId={}, timeoutTicks={}", companionEntry.type(), companionEntry.uuid(), 300);
    }

    private void moveAnimalTo(BlockPos teleportPos, CompanionEntry entry)
    {
        LOGGER.debug("Follow-player task completed: companionType={}, companionId={}", entry.type(), entry.uuid());
        if (animal.distanceToSqr(player) > MAX_ANIMAL_DISTANCE_TO_AVOID_TELEPORTING * MAX_ANIMAL_DISTANCE_TO_AVOID_TELEPORTING)
        {
            var teleported = tryTeleportAnimalTo(playerLevel, animal.getUUID(), teleportPos, entry.dimension(), true);
            if (teleported)
            {
                setGoal(animal, player);
                playTeleportSound(playerLevel, animal);
                showAnimalTeleportParticles(playerLevel, animal);

                if (teleportListener != null)
                    teleportListener.onTeleport(TeleportType.FOLLOW_PLAYER);
            }
            else
            {
                var name = CallCrystalHelper.buildAnimalName(entry.type(), entry.name());
                Players.sendOverlayMessage(player, Component.translatable("message.callofcompanions.cant_teleport", name));
                LOGGER.debug("Follow-player teleport failed: companion couldn't find a safe place to teleport, companionType={}, companionId={}", entry.type(), entry.uuid());
            }
        }
        else if (PREVENT_PETS_INVISIBILITY_WORKAROUND_1_21_1.isTrue() && animal.getType().is(EntityRegistry.ANIMALS_CAN_TELEPORT_TO_PLAYER))
        {
            recreateAnimal(playerLevel, animal, animal.getX(), animal.getY(), animal.getZ());
        }

        CallCrystalHelper.updateCompanionPos(playerLevel, companionData, entry);
    }
}
