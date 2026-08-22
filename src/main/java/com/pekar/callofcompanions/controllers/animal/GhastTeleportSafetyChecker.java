package com.pekar.callofcompanions.controllers.animal;

public class GhastTeleportSafetyChecker extends FlyingAnimalTeleportSafetyChecker
{
    @Override
    protected int getRequiredAirSpace()
    {
        return 3;
    }
}
