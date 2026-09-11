package com.pekar.callofcompanions;

import com.pekar.callofcompanions.config.ModConfigSpec;

public class ClientConfig
{
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec.BooleanValue SHOW_UPDATE_MESSAGE_ON_DISMOUNT;
    public static final ModConfigSpec.BooleanValue SHOW_UPDATE_MESSAGE_ON_INTERACT;
    public static final ModConfigSpec.ConfigValue<String> DATETIME_FORMAT;
    public static final ModConfigSpec.BooleanValue TOOLTIP_USE_REALTIME;
    public static final ModConfigSpec.BooleanValue TOOLTIP_AGE_COLORING;
    public static final ModConfigSpec.BooleanValue TOOLTIP_SHOW_LAST_POSITION;
    public static final ModConfigSpec.BooleanValue AUTO_UPDATE_ON_DISMOUNT;
    public static final ModConfigSpec.BooleanValue AUTO_UPDATE_ON_INTERACT;
    public static final ModConfigSpec.IntValue AUTO_UPDATE_DISTANCE_THRESHOLD;
    public static final ModConfigSpec SPEC;

    static
    {
        BUILDER.push("tracking");

        // When true, automatically update companion position when the player dismounts an animal.
        // This controls whether the mod will refresh stored companion positions on animal dismount events.
        AUTO_UPDATE_ON_DISMOUNT = BUILDER
                .comment("When dismounting, companion crystals in the player's inventory are checked automatically.",
                        "Only your crystals are updated. Disable both automatic-update options for manual control.",
                        "Manual updates and successful calls can still refresh coordinates.")
                .define("auto_update_on_dismount", true);

        // When true, automatically update companion position when the player interacts with an animal.
        // This controls whether the mod will refresh stored companion positions on animal interaction events.
        AUTO_UPDATE_ON_INTERACT = BUILDER
                .comment("When interacting with an animal, companion crystals in the player's inventory are checked automatically.",
                        "Only your crystals are updated. Disable both automatic-update options for manual control.",
                        "Manual updates and successful calls can still refresh coordinates.")
                .define("auto_update_on_interact", true);

        // Distance threshold (in blocks) used to determine whether an automatic update is needed.
        // If the stored companion position is within this many blocks of the actual animal
        // position, the mod will consider the stored position up-to-date and will not refresh it.
        AUTO_UPDATE_DISTANCE_THRESHOLD = BUILDER
                .comment("Distance threshold (in blocks) required to trigger an auto-update of the stored companion position.",
                        "Updates occur on dismount or interaction events.")
                .defineInRange("auto_update_distance_threshold_blocks", 32, 0, 1000);

        // When true, show an overlay message to the player when an automatic update occurs after dismounting.
        SHOW_UPDATE_MESSAGE_ON_DISMOUNT = BUILDER
                .comment("Show overlay message when companion position is auto-updated on dismount.")
                .define("show_update_message_on_dismount", true);

        // When true, show an overlay message to the player when an automatic update occurs after interacting.
        SHOW_UPDATE_MESSAGE_ON_INTERACT = BUILDER
                .comment("Show overlay message when companion position is auto-updated on interaction.")
                .define("show_update_message_on_interact", true);

        BUILDER.pop();

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

        // When true, tooltips will display the last saved world coordinates and dimension
        // for companions when the Alt key is held down.
        TOOLTIP_SHOW_LAST_POSITION = BUILDER
                .comment("If true, show last saved companion positions (coordinates + dimension) in tooltips when Alt is pressed.")
                .define("show_last_position", true);

        BUILDER.pop();

        SPEC = BUILDER.build();
    }
}
