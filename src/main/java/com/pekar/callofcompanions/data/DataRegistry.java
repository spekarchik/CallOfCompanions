package com.pekar.callofcompanions.data;

import net.minecraft.core.component.DataComponentType;
import net.neoforged.neoforge.registries.DeferredHolder;

import static com.pekar.callofcompanions.Main.DATA_COMPONENTS;

public class DataRegistry
{
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CompanionData>> COMPANIONS =
            DATA_COMPONENTS.register("companions", () ->
                    DataComponentType.<CompanionData>builder()
                            .persistent(CompanionData.CODEC)
                            .build()
            );

    public static void initStatic()
    {
        // just to initialize static members
    }
}
