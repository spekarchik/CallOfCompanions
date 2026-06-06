package com.pekar.callofcompanions.data;

import net.minecraft.core.UUIDUtil;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.UUID;

import static com.pekar.callofcompanions.Main.MODID;
import static com.pekar.callofcompanions.utils.Resources.createResourceLocation;

public class DataRegistry
{
    public static final DataComponentType<CompanionData> COMPANIONS = register(
            "companions",
            DataComponentType.<CompanionData>builder()
                    .persistent(CompanionData.CODEC)
                    .build()
    );

    public static final DataComponentType<UUID> CRYSTAL_ID = register(
            "crystal_id",
            DataComponentType.<UUID>builder()
                    .persistent(UUIDUtil.CODEC)
                    .build()
    );

    public static void initStatic()
    {
    }

    private static <T> DataComponentType<T> register(String name, DataComponentType<T> dataComponentType)
    {
        return Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, createResourceLocation(MODID, name), dataComponentType);
    }
}
