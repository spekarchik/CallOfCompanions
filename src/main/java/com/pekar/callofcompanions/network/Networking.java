package com.pekar.callofcompanions.network;

import com.pekar.callofcompanions.network.base.Packet;
import com.pekar.callofcompanions.network.base.PacketInfoProvider;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class Networking
{
    private static boolean initialized = false;
    private static boolean clientInitialized = false;

    private Networking()
    {}

    public static void init()
    {
        if (initialized) return;
        initialized = true;

        register(new SaveCompanionsPacket());
    }

    public static void initClient()
    {
        if (clientInitialized) return;
        clientInitialized = true;

        NetworkingClient.initClientReceivers();
    }

    private static <T extends Packet> void register(T packet)
    {
        var packetInfo = new PacketInfoProvider<>(packet);

        if (packet.isServerToClient())
        {
            PayloadTypeRegistry.clientboundPlay().register(packetInfo.getType(), packetInfo.getStreamCodec());
        }
        else
        {
            PayloadTypeRegistry.serverboundPlay().register(packetInfo.getType(), packetInfo.getStreamCodec());
            ServerPlayNetworking.registerGlobalReceiver(packetInfo.getType(), (payload, context) -> payload.onReceive(context.player()));
        }
    }
}

