package com.pekar.callofcompanions.utils;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public class Dimensions
{
    Dimensions()
    {

    }

    public static boolean isOverworld(ResourceKey<Level> dimension)
    {
        return dimension.identifier().equals(Level.OVERWORLD.identifier());
    }

    public static boolean isNether(ResourceKey<Level> dimension)
    {
        return dimension.identifier().equals(Level.NETHER.identifier());
    }

    public static boolean isEnd(ResourceKey<Level> dimension)
    {
        return dimension.identifier().equals(Level.END.identifier());
    }
}
