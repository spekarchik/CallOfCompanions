package com.pekar.callofcompanions.network;

import com.pekar.callofcompanions.events.IEventHandler;
import com.pekar.callofcompanions.network.base.Packet;
import com.pekar.callofcompanions.network.base.PacketInfoProvider;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.HandlerThread;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class NetworkingEventHandler implements IEventHandler
{
    public NetworkingEventHandler()
    {
    }

    @SubscribeEvent
    public void register(final RegisterPayloadHandlersEvent event)
    {
        final PayloadRegistrar registrar = event.registrar("1")
                .executesOn(HandlerThread.NETWORK);

        registerPacket(registrar, new SaveCompanionsPacket());
    }

    private <T extends Packet> void registerPacket(PayloadRegistrar registrar, T packet)
    {
        var packetInfoProvider = new PacketInfoProvider<>(packet);

        if (packet.isServerToClient())
        {
            registrar.playToClient(packetInfoProvider.getType(), packetInfoProvider.getStreamCodec(), packetInfoProvider.getHandler());
        }
        else
        {
            registrar.playToServer(packetInfoProvider.getType(), packetInfoProvider.getStreamCodec(), packetInfoProvider.getHandler());
        }
    }
}
