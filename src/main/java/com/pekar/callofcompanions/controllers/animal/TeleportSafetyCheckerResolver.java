package com.pekar.callofcompanions.controllers.animal;

import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Strider;

public class TeleportSafetyCheckerResolver
{
    public static TeleportSafetyChecker getChecker(Animal animal)
    {
        if (animal instanceof Strider) return new StriderTeleportSafetyChecker();
        else return new GroundAnimalTeleportSafetyChecker();
    }
}
