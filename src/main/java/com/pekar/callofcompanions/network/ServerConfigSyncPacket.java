package com.pekar.callofcompanions.network;

import com.pekar.callofcompanions.clientaccess.ClientAccessor;
import com.pekar.callofcompanions.network.base.IPacket;
import com.pekar.callofcompanions.network.base.ServerToClientPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;

public class ServerConfigSyncPacket extends ServerToClientPacket
{
    private final Map<String, String> values;

    ServerConfigSyncPacket()
    {
        this(Map.of());
    }

    public ServerConfigSyncPacket(Map<String, String> values)
    {
        this.values = Map.copyOf(values);
    }

    @Override
    public void onReceive(Player player)
    {
        ClientAccessor.networkAccessor().applyServerConfig(values);
    }

    @Override
    public void encode(FriendlyByteBuf buffer)
    {
        buffer.writeVarInt(values.size());
        values.forEach((name, value) -> {
            buffer.writeUtf(name, 128);
            buffer.writeUtf(value, 32);
        });
    }

    @Override
    public String getPacketId()
    {
        return Packets.ServerConfigSyncPacketId;
    }

    @Override
    public IPacket decode(FriendlyByteBuf buffer)
    {
        int count = buffer.readVarInt();
        if (count < 0 || count > 64) throw new IllegalArgumentException("Invalid config option count");
        Map<String, String> values = new HashMap<>();
        for (int i = 0; i < count; i++)
        {
            if (values.put(buffer.readUtf(128), buffer.readUtf(32)) != null)
                throw new IllegalArgumentException("Duplicate config option");
        }
        return new ServerConfigSyncPacket(values);
    }
}
