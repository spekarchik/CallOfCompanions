package com.pekar.callofcompanions;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config
{
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // When true, using a Call Crystal to summon animals will consume 1 XP level from the player.
    // Set to false to disable XP consumption when calling companions.
    public static final ModConfigSpec.BooleanValue CONSUME_XP_ON_CALL = BUILDER
            .comment("If true, calling animals with a call crystal consumes XP.")
            .define("consume_xp_on_call", true);

    // How many XP levels to consume when calling companions (only used when CONSUME_XP_ON_CALL is true).
    // Minimum 1, maximum 100.
    public static final ModConfigSpec.IntValue XP_LEVELS_TO_CONSUME = BUILDER
            .comment("Amount of XP levels consumed when calling companions")
            .defineInRange("xp_levels_to_consume", 1, 1, 100);

    // Radius in chunks to load around a companion when performing a far teleport.
    // WARNING: Increasing this value will make the server load more chunks (costly in memory and CPU).
    // Lower values may cause animals to not be found in remote chunks. Change at your own risk.
    // Valid range: 1..10 (1 = minimal, 10 = large area).
    public static final ModConfigSpec.IntValue FAR_TELEPORT_CHUNK_RADIUS = BUILDER
            .comment("Radius in chunks to load around a companion when doing a far teleport. Change at your own risk; higher values increase server memory/CPU usage.")
            .defineInRange("far_teleport_chunk_radius", 4, 1, 10);

    // How many companions a normal Call Crystal can store.
    // Increase at your own risk: higher values may increase memory usage.
    public static final ModConfigSpec.IntValue CRYSTAL_DATA_CAPACITY = BUILDER
            .comment("Number of companions a Call Crystal can store.")
            .defineInRange("crystal_data_capacity", 4, 1, 32);

    // How many companions a Deep Call Crystal can store.
    // Increase at your own risk: higher values may increase memory usage.
    public static final ModConfigSpec.IntValue DEEP_CRYSTAL_DATA_CAPACITY = BUILDER
            .comment("Number of companions a Deep Call Crystal can store.")
            .defineInRange("deep_crystal_data_capacity", 8, 1, 32);

    static final ModConfigSpec SPEC = BUILDER.build();
}
