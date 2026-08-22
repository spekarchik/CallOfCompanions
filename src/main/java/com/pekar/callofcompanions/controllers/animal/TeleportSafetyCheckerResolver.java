package com.pekar.callofcompanions.controllers.animal;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.animal.AgeableWaterCreature;
import net.minecraft.world.entity.animal.happyghast.HappyGhast;
import net.minecraft.world.entity.animal.nautilus.AbstractNautilus;
import net.minecraft.world.entity.monster.Strider;
import net.minecraft.world.level.pathfinder.PathType;

public class TeleportSafetyCheckerResolver
{
    public static TeleportSafetyChecker getChecker(PathfinderMob animal)
    {
        if (animal instanceof AbstractNautilus
                || animal instanceof AgeableWaterCreature
                || animal.getNavigation() instanceof WaterBoundPathNavigation
                || (animal.getPathfindingMalus(PathType.WATER) == 0f && animal.getPathfindingMalus(PathType.WALKABLE) != 0f)
        )
            return new WaterAnimalTeleportSafetyChecker();

        else if (animal instanceof HappyGhast)
            return new GhastTeleportSafetyChecker();

        else if (animal.getNavigation() instanceof FlyingPathNavigation)
            return new FlyingAnimalTeleportSafetyChecker();

        else if (animal instanceof Strider
                || (animal.getPathfindingMalus(PathType.LAVA) == 0f && animal.getPathfindingMalus(PathType.WALKABLE) != 0f)
        )
            return new LavaAnimalTeleportSafetyChecker();

        else
            return new GroundAnimalTeleportSafetyChecker();
    }

    public static TeleportSafetyChecker getAlternativeChecker(PathfinderMob animal)
    {
        var primaryChecker = getChecker(animal);
        if (primaryChecker instanceof WaterAnimalTeleportSafetyChecker) return null;

        if (!(primaryChecker instanceof GroundAnimalTeleportSafetyChecker) && animal.getPathfindingMalus(PathType.WALKABLE) == 0f)
            return new GroundAnimalTeleportSafetyChecker();
        if (!(primaryChecker instanceof WaterAnimalTeleportSafetyChecker) && animal.getPathfindingMalus(PathType.WATER) == 0f)
            return new WaterAnimalTeleportSafetyChecker();
        if (!(primaryChecker instanceof LavaAnimalTeleportSafetyChecker) && animal.getPathfindingMalus(PathType.LAVA) == 0f)
            return new LavaAnimalTeleportSafetyChecker();

        return null;
    }
}
