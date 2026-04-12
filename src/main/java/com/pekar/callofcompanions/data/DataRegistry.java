package com.pekar.callofcompanions.data;

import com.pekar.callofcompanions.Config;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentType;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.UUID;

import static com.pekar.callofcompanions.Main.DATA_COMPONENTS;

public class DataRegistry
{
    // These values are initialized from the runtime configuration in initStatic()
    public static short CRYSTAL_DATA_CAPACITY = 4;
    public static short DEEP_CRYSTAL_DATA_CAPACITY = 8;

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CompanionData>> COMPANIONS =
            DATA_COMPONENTS.register("companions", () ->
                    DataComponentType.<CompanionData>builder()
                            .persistent(CompanionData.CODEC)
                            .build()
            );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<UUID>> CRYSTAL_ID =
            DATA_COMPONENTS.register("crystal_id", () ->
                    DataComponentType.<UUID>builder()
                            .persistent(UUIDUtil.CODEC)
                            .build()
            );

    public static void initStatic()
    {
        // Initialize capacities from config so they can be changed by server owners
        try
        {
            int c = Config.CRYSTAL_DATA_CAPACITY.getAsInt();
            int d = Config.DEEP_CRYSTAL_DATA_CAPACITY.getAsInt();

            CRYSTAL_DATA_CAPACITY = (short) c;
            DEEP_CRYSTAL_DATA_CAPACITY = (short) d;
        }
        catch (Throwable t)
        {
            // In case config isn't ready yet or an error occurs, keep defaults
        }
    }
}
