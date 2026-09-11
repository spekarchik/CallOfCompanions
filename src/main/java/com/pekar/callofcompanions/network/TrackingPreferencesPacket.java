package com.pekar.callofcompanions.network;

import com.pekar.callofcompanions.controllers.config.TrackingPreferencesController;
import com.pekar.callofcompanions.data.TrackingPreferences;
import com.pekar.callofcompanions.network.base.ClientToServerPacket;
import com.pekar.callofcompanions.network.base.IPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class TrackingPreferencesPacket extends ClientToServerPacket
{
    private final TrackingPreferences trackingPreferences;

    TrackingPreferencesPacket()
    {
        this(TrackingPreferences.DEFAULT);
    }

    public TrackingPreferencesPacket(TrackingPreferences trackingPreferences)
    {
        this.trackingPreferences = trackingPreferences;
    }

    @Override
    public void onReceive(ServerPlayer player)
    {
        TrackingPreferencesController.set(player.getUUID(), trackingPreferences);
    }

    @Override
    public void encode(FriendlyByteBuf buffer)
    {
        buffer.writeBoolean(trackingPreferences.autoUpdateOnDismount());
        buffer.writeBoolean(trackingPreferences.autoUpdateOnInteract());
        buffer.writeVarInt(trackingPreferences.distanceThreshold());
    }

    @Override
    public String getPacketId()
    {
        return Packets.TrackingPreferencesPacketId;
    }

    @Override
    public IPacket decode(FriendlyByteBuf buffer)
    {
        var prefs = new TrackingPreferences(buffer.readBoolean(), buffer.readBoolean(), buffer.readVarInt());
        return new TrackingPreferencesPacket(prefs);
    }
}
