package com.pekar.callofcompanions.utils;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class Players
{
    private Players()
    {}

    public static void sendOverlayMessage(ServerPlayer player, Component message)
    {
        player.sendSystemMessage(message, true);
    }
}
