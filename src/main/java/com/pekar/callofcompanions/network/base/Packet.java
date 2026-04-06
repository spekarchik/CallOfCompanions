package com.pekar.callofcompanions.network.base;

import com.pekar.callofcompanions.Main;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import static com.pekar.callofcompanions.utils.Resources.createResourceLocation;

public abstract class Packet implements IPacket, CustomPacketPayload
{
    private Type<Packet> type;

    protected Packet()
    {
    }

    final void handlePacket(final IPayloadContext context)
    {
        context.enqueueWork(() -> onReceive(context))
                        .exceptionally(e ->
                        {
                            context.disconnect(Component.translatable(Main.MODID + " networking failed: ", e.getMessage()));
                            return null;
                        });
    }

    @Override
    public final Type<Packet> type()
    {
        return type == null
                ? (type = new Type<>(createResourceLocation(Main.MODID, getPacketId())))
                : type;
    }

    public abstract boolean isServerToClient();

    protected abstract void onReceive(IPayloadContext contextContainer);
}
