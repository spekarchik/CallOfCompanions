package com.pekar.callofcompanions.controllers;

import net.minecraft.world.entity.PathfinderMob;

abstract class LoadedAnimalSummonController extends AnimalSummonController
{
    protected final PathfinderMob animal;

    protected LoadedAnimalSummonController(SummonAnimalContext context)
    {
        super(context);
        this.animal = context.animal();
    }
}
