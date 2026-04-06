package com.pekar.callofcompanions.utils;

import net.minecraft.resources.Identifier;

public class Resources
{
    Resources()
    {

    }

    public static Identifier createResourceLocation(String namespace, String name)
    {
        return Identifier.fromNamespaceAndPath(namespace, name); //was: new ResourceLocation(name),
    }
}
