package com.pekar.callofcompanions.network.base;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.StreamDecoder;
import net.minecraft.network.codec.StreamEncoder;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

public class PacketInfoProvider<T extends Packet> implements IPacketInfoProvider<T>
{
    private final T packet;

    public PacketInfoProvider(T packet)
    {
        this.packet = packet;
    }

    @Override
    public CustomPacketPayload.Type<T> getType()
    {
        return (CustomPacketPayload.Type<T>) packet.type();
    }

    @Override
    public StreamCodec<FriendlyByteBuf, T> getStreamCodec()
    {
        return StreamCodec.of(getEncoder(), getDecoder());
    }

    @Override
    public IPayloadHandler<T> getHandler()
    {
        return getPacketHandler();
    }

    private StreamEncoder<FriendlyByteBuf, T> getEncoder()
    {
        return (buffer, packet) -> packet.encode(buffer);
    }

    private StreamDecoder<FriendlyByteBuf, T> getDecoder()
    {
        return buffer -> (T)packet.decode(buffer);
    }

    private IPayloadHandler<T> getPacketHandler()
    {
        return (packet, context) -> packet.handlePacket(context);
    }
}
