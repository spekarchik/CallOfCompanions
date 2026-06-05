package com.pekar.callofcompanions.network;

import com.pekar.callofcompanions.network.base.Packet;
import com.pekar.callofcompanions.network.base.PacketInfoProvider;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

@Environment(EnvType.CLIENT)
final class NetworkingClient
{
    private NetworkingClient()
    {}

    static void initClientReceivers()
    {
        registerClientbound(new SaveCompanionsPacket());
    }

    private static <T extends Packet> void registerClientbound(T packet)
    {
        if (!packet.isServerToClient()) return;

        var packetInfo = new PacketInfoProvider<>(packet);
        ClientPlayNetworking.registerGlobalReceiver(packetInfo.getType(), (payload, context) -> payload.onReceive(context.player()));
    }
}
