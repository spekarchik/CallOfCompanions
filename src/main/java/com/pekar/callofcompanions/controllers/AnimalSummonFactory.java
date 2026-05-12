package com.pekar.callofcompanions.controllers;

import com.pekar.callofcompanions.entity.EntityRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;

public class AnimalSummonFactory
{
    public static AnimalSummonController get(SummonAnimalContext context)
    {
        switch (resolveTeleportType(context.player(), context.playerLevel(), context.animal(), context.companionEntry().dimension()))
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
            case FAR_TELEPORT, CROSS_DIMENSION_TELEPORT -> {
                return new FarTeleportController(context);
            }
        }
        throw new IllegalStateException("Unexpected teleport type");
    }

    private static TeleportType resolveTeleportType(ServerPlayer player, ServerLevel playerLevel, Animal animal, ResourceKey<Level> animalDimension)
    {
        final double MAX_DIST_FOR_GOAL_SQR = 32 * 32;
        final double MIN_DIST_FOR_VANILLA_TELEPORT_SQR = 11 * 11;

        if (!playerLevel.dimension().equals(animalDimension))
            return TeleportType.CROSS_DIMENSION_TELEPORT;

        if (animal == null) return TeleportType.FAR_TELEPORT;

        var distanceSqr = player.distanceToSqr(animal);
        if (animal.is(EntityRegistry.ANIMALS_CAN_TELEPORT_TO_PLAYER) && distanceSqr > MIN_DIST_FOR_VANILLA_TELEPORT_SQR)
            return TeleportType.VANILLA_TELEPORT;

        if (distanceSqr < MAX_DIST_FOR_GOAL_SQR) return TeleportType.FOLLOW_PLAYER;
        return TeleportType.NEAR_TELEPORT;
    }
}
