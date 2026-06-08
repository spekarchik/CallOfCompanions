package com.pekar.callofcompanions.controllers.animal;

import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.monster.Strider;
import net.minecraft.world.level.pathfinder.PathType;

public class TeleportSafetyCheckerResolver
{
    public static TeleportSafetyChecker getChecker(Animal animal)
    {
        if (animal instanceof Axolotl
                || animal.getNavigation() instanceof WaterBoundPathNavigation
                || (animal.getPathfindingMalus(PathType.WATER) == 0f && animal.getPathfindingMalus(PathType.WALKABLE) != 0f)
        )
            return new WaterAnimalTeleportSafetyChecker();

        else if (animal instanceof Strider
                || (animal.getPathfindingMalus(PathType.LAVA) == 0f && animal.getPathfindingMalus(PathType.WALKABLE) != 0f)
        )
            return new LavaAnimalTeleportSafetyChecker();

        else
            return new GroundAnimalTeleportSafetyChecker();
    }

    public static TeleportSafetyChecker getAlternativeChecker(Animal animal)
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
