package com.pekar.callofcompanions.controllers;

import com.pekar.callofcompanions.entity.EntityRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.animal.Animal;

public class AnimalSummonFactory
{
    private enum TeleportType
    {
        VANILLA_TELEPORT,
        FOLLOW_PLAYER,
        NEAR_TELEPORT,
        FAR_TELEPORT
    }

    public static AnimalSummonController get(SummonAnimalContext context)
    {
        switch (resolveTeleportType(context.player(), context.animal()))
        {
            case VANILLA_TELEPORT -> {
                return new VanillaTeleportController(context);
            }
            case FOLLOW_PLAYER -> {
                return new FollowPlayerController(context);
            }
            case NEAR_TELEPORT -> {
                return new NearTeleportController(context);
            }
            case FAR_TELEPORT -> {
                return new FarTeleportController(context);
            }
        }
        throw new IllegalStateException("Unexpected teleport type");
    }

    private static TeleportType resolveTeleportType(ServerPlayer serverPlayer, Animal animal)
    {
        final double MAX_DIST_FOR_GOAL_SQR = 32 * 32;
        final double MIN_DIST_FOR_VANILLA_TELEPORT_SQR = 11 * 11;

        if (animal == null) return TeleportType.FAR_TELEPORT;

        var distanceSqr = serverPlayer.distanceToSqr(animal);
        if (animal.is(EntityRegistry.ANIMALS_CAN_TELEPORT_TO_PLAYER) && distanceSqr > MIN_DIST_FOR_VANILLA_TELEPORT_SQR)
            return TeleportType.VANILLA_TELEPORT;

        if (distanceSqr < MAX_DIST_FOR_GOAL_SQR) return TeleportType.FOLLOW_PLAYER;
        return TeleportType.NEAR_TELEPORT;
    }
}
