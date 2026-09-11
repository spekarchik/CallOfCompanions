package com.pekar.callofcompanions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

final class ConfigMigration
{
    private ConfigMigration()
    {}

    static void migrate(Path configDirectory, String side)
    {
        Path legacy = configDirectory.resolve(Main.MODID + "-common.toml");
        Path target = configDirectory.resolve(Main.MODID + "-" + side + ".toml");
        if (!Files.exists(legacy) || Files.exists(target)) return;

        try
        {
            // Keep the original as a backup. NeoForge removes keys outside the new spec on load.
            Files.copy(legacy, target);
        }
        catch (IOException e)
        {
            throw new IllegalStateException("Failed to migrate config from " + legacy + " to " + target, e);
        }
    }
}
