package com.pekar.callofcompanions.network.base;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;

@Environment(EnvType.CLIENT)
final class ClientSidePacketHelper
{
    static ClientPacketListener getConnection()
    {
        return Minecraft.getInstance().getConnection();
    }
}
