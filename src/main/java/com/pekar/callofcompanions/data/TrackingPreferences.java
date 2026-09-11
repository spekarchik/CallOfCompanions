package com.pekar.callofcompanions.data;

public record TrackingPreferences(boolean autoUpdateOnDismount, boolean autoUpdateOnInteract, int distanceThreshold)
{
    public static final TrackingPreferences DEFAULT = new TrackingPreferences(false, false, 32);

    public TrackingPreferences
    {
        distanceThreshold = Math.clamp(distanceThreshold, 0, 1000);
    }
}
