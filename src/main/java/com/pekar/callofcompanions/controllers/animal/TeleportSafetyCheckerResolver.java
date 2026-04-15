package com.pekar.callofcompanions.controllers.animal;

import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.happyghast.HappyGhast;
import net.minecraft.world.entity.animal.nautilus.AbstractNautilus;
import net.minecraft.world.entity.monster.Strider;

public class TeleportSafetyCheckerResolver
{
    public static TeleportSafetyChecker getChecker(Animal animal)
    {
        if (animal instanceof AbstractNautilus) return new WaterAnimalTeleportSafetyChecker();
        else if (animal instanceof HappyGhast) return new GhastTeleportSafetyChecker();
        else if (animal instanceof Strider) return new StriderTeleportSafetyChecker();
        else return new GroundAnimalTeleportSafetyChecker();
    }
}
