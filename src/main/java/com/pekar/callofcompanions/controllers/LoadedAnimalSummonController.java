package com.pekar.callofcompanions.controllers;

import net.minecraft.world.entity.animal.Animal;

abstract class LoadedAnimalSummonController extends AnimalSummonController
{
    protected final Animal animal;

    protected LoadedAnimalSummonController(SummonAnimalContext context)
    {
        super(context);
        this.animal = context.animal();
    }
}
