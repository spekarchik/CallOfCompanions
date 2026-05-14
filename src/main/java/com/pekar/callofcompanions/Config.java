package com.pekar.callofcompanions;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config
{
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.BooleanValue CONSUME_XP_ON_CALL;
    public static final ModConfigSpec.IntValue XP_LEVELS_TO_CONSUME;
    public static final ModConfigSpec.IntValue XP_LEVELS_TO_CONSUME_CROSS_DIMENSION;
    public static final ModConfigSpec.IntValue FAR_TELEPORT_CHUNK_RADIUS;
    public static final ModConfigSpec.IntValue FAR_TELEPORT_WAIT_TICKS;
    public static final ModConfigSpec.IntValue CRYSTAL_DATA_CAPACITY;
    public static final ModConfigSpec.IntValue DEEP_CRYSTAL_DATA_CAPACITY;
    public static final ModConfigSpec.IntValue CRYSTAL_COOLDOWN;
    public static final ModConfigSpec.IntValue DEEP_CRYSTAL_COOLDOWN;
    public static final ModConfigSpec.BooleanValue DEEP_CRYSTAL_ALLOW_UNTAMED;
    public static final ModConfigSpec.BooleanValue DEEP_CRYSTAL_ALLOW_INTERDIMENSIONAL;
    public static final ModConfigSpec.DoubleValue DEEP_CRYSTAL_CROSS_DIMENSION_DELAY_MULTIPLIER;
    public static final ModConfigSpec.ConfigValue<String> DATETIME_FORMAT;
    public static final ModConfigSpec.BooleanValue TOOLTIP_USE_REALTIME;
    public static final ModConfigSpec.BooleanValue TOOLTIP_AGE_COLORING;
    public static final ModConfigSpec.BooleanValue PREVENT_PETS_INVISIBILITY_WORKAROUND_1_21_1;

    static
    {
        // -----------------------------------

        BUILDER.push("calling");

        // If true, calling companions from unloaded chunks or other dimensions
        // consumes XP levels from the player.
        //
        // No XP is consumed when companions are already loaded
        // and can simply navigate or teleported to the player normally.
        CONSUME_XP_ON_CALL = BUILDER
                .comment("If true, teleporting companions from unloaded chunks or other dimensions consumes XP.")
                .define("consume_xp_on_call", true);

        // Amount of XP levels consumed when at least one companion
        // is teleported from an unloaded chunk or another dimension.
        //
        // XP is consumed once per call crystal use,
        // regardless of how many companions were teleported.
        XP_LEVELS_TO_CONSUME = BUILDER
                .comment("XP levels consumed when companions are teleported from unloaded chunks or other dimensions.")
                .defineInRange("xp_levels_to_consume", 1, 0, 100);

        // Separate settings for cross-dimensional calls (teleports between dimensions).
        // Cross-dimensional calls may be more costly, so these default to 4 XP levels.
        XP_LEVELS_TO_CONSUME_CROSS_DIMENSION = BUILDER
                .comment("XP levels consumed when companions are teleported across dimensions.")
                .defineInRange("xp_levels_to_consume_cross_dimension", 4, 1, 100);

        BUILDER.pop();

        // -----------------------------------

        BUILDER.push("call_crystal");

        // How many companions a normal Call Crystal can store.
        // Increase at your own risk: higher values may increase memory usage.
        CRYSTAL_DATA_CAPACITY = BUILDER
                .comment("Number of companions a Call Crystal can store.")
                .defineInRange("data_capacity", 4, 1, 32);

        // Cooldown (in ticks) applied to using a normal Call Crystal.
        // Default 600 ticks (30 seconds). Range: 0 = no cooldown .. 72000 (1 hour)
        CRYSTAL_COOLDOWN = BUILDER
                .comment("Cooldown in ticks for using a Call Crystal. 20 ticks = 1 second")
                .defineInRange("cooldown_ticks", 600, 0, 72000);

        BUILDER.pop();

        // -----------------------------------

        BUILDER.push("deep_call_crystal");

        // How many companions a Deep Call Crystal can store.
        // Increase at your own risk: higher values may increase memory usage.
        DEEP_CRYSTAL_DATA_CAPACITY = BUILDER
                .comment("Number of companions a Deep Call Crystal can store.")
                .defineInRange("data_capacity", 8, 1, 32);

        // Cooldown (in ticks) applied to using a Deep Call Crystal.
        // Default 400 ticks (20 seconds). Range: 0 = no cooldown .. 72000 (1 hour)
        DEEP_CRYSTAL_COOLDOWN = BUILDER
                .comment("Cooldown in ticks for using a Deep Call Crystal. 20 ticks = 1 second")
                .defineInRange("cooldown_ticks", 400, 0, 72000);

        // When true, Deep Call Crystal will allow adding untamed animals (if they are named).
        // Default is true to preserve previous behavior where named untamed animals could be bound.
        // Set to false to restrict Deep Call Crystals to tamed animals only.
        DEEP_CRYSTAL_ALLOW_UNTAMED = BUILDER
                .comment("If false, Deep Call Crystals cannot add untamed animals (even if named).")
                .define("allow_untamed", true);

        // When true, Deep Call Crystal will allow calling animals across dimensions (cross-dimensional teleports).
        // Default is true to preserve previous behavior where deep crystals allowed inter-dimensional teleports.
        DEEP_CRYSTAL_ALLOW_INTERDIMENSIONAL = BUILDER
                .comment("If true, Deep Call Crystals allow calling animals from other dimensions.")
                .define("allow_interdimensional", true);

        // Additional multiplier applied to deep-call waiting time when the companion is in another dimension.
        // For example, a value of 2.0 will double the random waiting interval used for same-dimension calls.
        DEEP_CRYSTAL_CROSS_DIMENSION_DELAY_MULTIPLIER = BUILDER
                .comment("Multiplier applied to waiting time for cross-dimensional calls.")
                .defineInRange("cross_dimension_delay_multiplier", 4.0D, 0.0D, 20.0D);

        BUILDER.pop();

        // -----------------------------------

        BUILDER.push("tooltip");

        // Date/time format used in tooltips and displays.
        // Uses java.time.format.DateTimeFormatter patterns. Default is en-US style: MM/dd/yyyy HH:mm
        DATETIME_FORMAT = BUILDER
                .comment("Date/time format used in tooltips. Uses java.time.format.DateTimeFormatter patterns. Default: MM/dd/yyyy HH:mm")
                .define("datetime_format", "MM/dd/yyyy HH:mm");

        // Controls how relative time is displayed in Crystal tooltips.
        // false = Minecraft in-game time (20 real minutes = 1 in-game day)
        // true = real-world elapsed time
        TOOLTIP_USE_REALTIME = BUILDER
                .comment(
                        "Controls how relative time is displayed in Crystal tooltips.",
                        "false = Minecraft in-game time (20 real minutes = 1 in-game day)",
                        "        Time does not progress while the world/server is offline.",
                        "true = real-world elapsed time"
                )
                .define("always_use_realtime", false);

        // When true, tooltips will color companion lines depending on the stored companion data age.
        // The in-code age thresholds are:
        //  - GREEN:  age <= 2 minutes  (<= 120_000 ms)
        //  - WHITE:  2 minutes < age <= 20 minutes (<= 1_200_000 ms)
        // For older data the tooltip falls back to the default styling (or other styles like dark gray for LOST entries).
        // If false, age-based coloring will be disabled and the tooltip lines will use default styling.
        TOOLTIP_AGE_COLORING = BUILDER
                .comment("Enable/disable coloring tooltip lines depending on stored companion data age.")
                .comment("Green when age <= 2 minutes; white when >2 and <=20 minutes.")
                .define("age_coloring", true);

        BUILDER.pop();

        // -----------------------------------

        BUILDER.push("technical");

        // Radius in chunks to load around a companion when performing a far teleport.
        // WARNING: Increasing this value will make the server load more chunks (costly in memory and CPU).
        // Lower values may cause animals to not be found in remote chunks. Change at your own risk.
        // Valid range: 1..32 (1 = minimal, 12 = large area).
        FAR_TELEPORT_CHUNK_RADIUS = BUILDER
                .comment("Radius in chunks to load around a companion when doing a far teleport. Change at your own risk; higher values increase server memory/CPU usage.")
                .defineInRange("far_teleport_chunk_radius", 6, 1, 32);

        // How many ticks the far-teleport task will wait while trying to load/find the companion
        // before giving up. This controls the timeout used when searching for the entity
        // after scheduling chunk loading.
        FAR_TELEPORT_WAIT_TICKS = BUILDER
                .comment("Maximum wait time for chunk loading and companion search task. 20 ticks = 1 second")
                .defineInRange("far_teleport_wait_ticks", 100, 1, 12000);

        // Workaround for a Minecraft 1.21.1 bug where dogs, cats or parrots sometimes appear invisible after teleporting.
        // When true, the mod will recreate the animal after calling to prevent invisibility. This prevents the
        // invisibility but causes a noticeable single position shift/jump when calling the same type of animal
        // and the animal is nearby (the animal comes to the player and is recreated to avoid the invisibility).
        // Default: false. Specific to Minecraft 1.21.1; enable only if players observe the invisible-animal issue.
        PREVENT_PETS_INVISIBILITY_WORKAROUND_1_21_1 = BUILDER
                .comment("Workaround for MC 1.21.1: recreate dogs/cats/parrots on call to avoid invisibility after teleporting; causes a single position jump.")
                .define("prevent_pets_invisibility_workaround_1_21_1", false);

        BUILDER.pop();

        // -----------------------------------

        SPEC = BUILDER.build();
    }
}
