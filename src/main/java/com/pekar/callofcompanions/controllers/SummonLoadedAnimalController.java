package com.pekar.callofcompanions.controllers;

import net.minecraft.world.entity.animal.Animal;

abstract class SummonLoadedAnimalController extends SummonAnimalController
{
    protected final Animal animal;

    protected SummonLoadedAnimalController(SummonAnimalContext context)
    {
        super(context);
        this.animal = context.animal();
    }
}
