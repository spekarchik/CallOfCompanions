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
    // Valid range: 1..12 (1 = minimal, 12 = large area).
    public static final ModConfigSpec.IntValue FAR_TELEPORT_CHUNK_RADIUS = BUILDER
            .comment("Radius in chunks to load around a companion when doing a far teleport. Change at your own risk; higher values increase server memory/CPU usage.")
            .defineInRange("far_teleport_chunk_radius", 6, 1, 12);

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

    // Cooldown (in ticks) applied to using a normal Call Crystal.
    // Default 600 ticks (30 seconds). Range: 0 = no cooldown .. 72000 (1 hour)
    public static final ModConfigSpec.IntValue CRYSTAL_COOLDOWN = BUILDER
            .comment("Cooldown in ticks for using a Call Crystal. 20 ticks = 1 second")
            .defineInRange("crystal_cooldown_ticks", 600, 0, 72000);

    // Cooldown (in ticks) applied to using a Deep Call Crystal.
    // Default 400 ticks (20 seconds). Range: 0 = no cooldown .. 72000 (1 hour)
    public static final ModConfigSpec.IntValue DEEP_CRYSTAL_COOLDOWN = BUILDER
            .comment("Cooldown in ticks for using a Deep Call Crystal. 20 ticks = 1 second")
            .defineInRange("deep_crystal_cooldown_ticks", 400, 0, 72000);

    // When true, Deep Call Crystal will NOT allow adding untamed animals (even if they are named).
    // Default is false to preserve previous behavior. Set to true to restrict Deep Call Crystals to tamed animals only.
    public static final ModConfigSpec.BooleanValue DEEP_CRYSTAL_DISALLOW_UNTAMED = BUILDER
            .comment("If true, Deep Call Crystals cannot add untamed animals (even if named).")
            .define("deep_crystal_disallow_untamed", false);

    // Workaround for a Minecraft 1.21.1 bug where dogs, cats or parrots sometimes appear invisible after teleporting.
    // When true, the mod will recreate the animal after calling to prevent invisibility. This prevents the
    // invisibility but causes a noticeable single position shift/jump when calling the same type of animal
    // and the animal is nearby (the animal comes to the player and is recreated to avoid the invisibility).
    // Default: false. Specific to Minecraft 1.21.1; enable only if players observe the invisible-animal issue.
    public static final ModConfigSpec.BooleanValue PREVENT_PETS_INVISIBILITY_WORKAROUND_1_21_1 = BUILDER
            .comment("Workaround for MC 1.21.1: recreate dogs/cats/parrots on call to avoid invisibility; causes a single position jump.")
            .define("prevent_pets_invisibility_workaround_1_21_1", false);

    // Date/time format used in tooltips and displays.
    // Uses java.time.format.DateTimeFormatter patterns. Default is en-US style: MM/dd/yyyy HH:mm
    public static final ModConfigSpec.ConfigValue<String> DATETIME_FORMAT = BUILDER
            .comment("Date/time format used in tooltips. Uses java.time.format.DateTimeFormatter patterns. Default: MM/dd/yyyy HH:mm")
            .define("datetime_format", "MM/dd/yyyy HH:mm");

    // When true, tooltips will color companion lines depending on the stored companion data age.
    // The in-code age thresholds are:
    //  - GREEN:  age <= 2 minutes  (<= 120_000 ms)
    //  - WHITE:  2 minutes < age <= 20 minutes (<= 1_200_000 ms)
    // For older data the tooltip falls back to the default styling (or other styles like dark gray for LOST entries).
    // If false, age-based coloring will be disabled and the tooltip lines will use default styling.
    public static final ModConfigSpec.BooleanValue TOOLTIP_AGE_COLORING = BUILDER
            .comment("Enable/disable coloring tooltip lines depending on stored companion data age.")
            .comment("Green when age <= 2 minutes; white when >2 and <=20 minutes.")
            .define("tooltip_age_coloring", true);

    static final ModConfigSpec SPEC = BUILDER.build();
}
