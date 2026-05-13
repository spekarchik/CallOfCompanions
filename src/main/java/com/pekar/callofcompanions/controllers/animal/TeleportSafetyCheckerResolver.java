package com.pekar.callofcompanions.controllers.animal;

import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.animal.happyghast.HappyGhast;
import net.minecraft.world.entity.animal.nautilus.AbstractNautilus;
import net.minecraft.world.entity.monster.Strider;
import net.minecraft.world.level.pathfinder.PathType;

public class TeleportSafetyCheckerResolver
{
    public static TeleportSafetyChecker getChecker(Animal animal)
    {
        if (animal instanceof AbstractNautilus
                || animal instanceof Axolotl
                || animal.getNavigation() instanceof WaterBoundPathNavigation
                || (animal.getPathfindingMalus(PathType.WATER) == 0f && animal.getPathfindingMalus(PathType.WALKABLE) != 0f)
        )
            return new WaterAnimalTeleportSafetyChecker();

        else if (animal instanceof HappyGhast)
            return new GhastTeleportSafetyChecker();
        else if (animal instanceof Strider
                || (animal.getPathfindingMalus(PathType.LAVA) == 0f && animal.getPathfindingMalus(PathType.WALKABLE) != 0f)
        )
            return new StriderTeleportSafetyChecker();
        else
            return new GroundAnimalTeleportSafetyChecker();
    }
}
