package com.pekar.callofcompanions.controllers;

public interface TeleportListener
{
    void onTeleport(TeleportType teleportType);
    boolean teleported();
}
