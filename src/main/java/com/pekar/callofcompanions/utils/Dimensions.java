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
        return dimension.location().equals(Level.OVERWORLD.location());
    }

    public static boolean isNether(ResourceKey<Level> dimension)
    {
        return dimension.location().equals(Level.NETHER.location());
    }

    public static boolean isEnd(ResourceKey<Level> dimension)
    {
        return dimension.location().equals(Level.END.location());
    }
}
