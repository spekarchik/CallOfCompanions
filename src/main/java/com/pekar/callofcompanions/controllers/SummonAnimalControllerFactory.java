package com.pekar.callofcompanions.controllers;

import com.pekar.callofcompanions.entity.EntityRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.animal.Animal;

public class SummonAnimalControllerFactory
{
    private enum TeleportType
    {
        TELEPORT_BY_VANILLA,
        GOAL_TO_PLAYER,
        NEAR_TELEPORT,
        FAR_TELEPORT
    }

    public static SummonAnimalController get(SummonAnimalContext context)
    {
        switch (teleportType(context.player(), context.animal()))
        {
            case TELEPORT_BY_VANILLA -> {
                return new VanillaTeleportAnimalController(context);
            }
            case GOAL_TO_PLAYER -> {
                return new GoalToPlayerAnimalController(context);
            }
            case NEAR_TELEPORT -> {
                return new NearTeleportAnimalController(context);
            }
            case FAR_TELEPORT -> {
                return new FarTeleportAnimalController(context);
            }
        }
        throw new IllegalStateException("Unexpected teleport type");
    }

    private static TeleportType teleportType(ServerPlayer serverPlayer, Animal animal)
    {
        final double MAX_DIST_FOR_GOAL_SQR = 32 * 32;
        final double MAX_DIST_FOR_NEAR_TELEPORT = 128 * 128;

        if (animal == null) return TeleportType.FAR_TELEPORT;
        if (animal.is(EntityRegistry.ANIMALS_CAN_TELEPORT_TO_PLAYER)) return TeleportType.TELEPORT_BY_VANILLA;

        var distanceSqr = serverPlayer.distanceToSqr(animal);
        if (distanceSqr < MAX_DIST_FOR_GOAL_SQR) return TeleportType.GOAL_TO_PLAYER;
        return distanceSqr < MAX_DIST_FOR_NEAR_TELEPORT ? TeleportType.NEAR_TELEPORT : TeleportType.FAR_TELEPORT;
    }
}
