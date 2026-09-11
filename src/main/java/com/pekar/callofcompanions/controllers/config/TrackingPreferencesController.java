package com.pekar.callofcompanions.controllers.config;

import com.pekar.callofcompanions.data.TrackingPreferences;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class TrackingPreferencesController
{
    private static final Map<UUID, TrackingPreferences> TRACKING_PREFERENCES = new HashMap<>();

    public static void set(UUID playerId, TrackingPreferences preferences)
    {
        TRACKING_PREFERENCES.put(playerId, preferences);
    }

    public static TrackingPreferences get(UUID playerId)
    {
        return TRACKING_PREFERENCES.getOrDefault(playerId, TrackingPreferences.DEFAULT);
    }

    public static void remove(UUID playerId)
    {
        TRACKING_PREFERENCES.remove(playerId);
    }

    public static void clear()
    {
        TRACKING_PREFERENCES.clear();
    }
}
