package com.pekar.callofcompanions.network.base;

import net.minecraft.network.FriendlyByteBuf;

public interface IPacket
{
    String getPacketId();
    default void encode(FriendlyByteBuf buffer) {}
    IPacket decode(FriendlyByteBuf buffer);
}
