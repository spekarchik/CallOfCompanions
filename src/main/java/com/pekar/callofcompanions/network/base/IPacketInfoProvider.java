package com.pekar.callofcompanions.network.base;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

public interface IPacketInfoProvider<T extends CustomPacketPayload>
{
    CustomPacketPayload.Type<T> getType();
    StreamCodec<FriendlyByteBuf, T> getStreamCodec();
    IPayloadHandler<T> getHandler();
}
