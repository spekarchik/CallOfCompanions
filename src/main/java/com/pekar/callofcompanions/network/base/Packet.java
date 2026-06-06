package com.pekar.callofcompanions.network.base;

import com.pekar.callofcompanions.Main;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

import static com.pekar.callofcompanions.utils.Resources.createResourceLocation;

public abstract class Packet implements IPacket, CustomPacketPayload
{
    private Type<Packet> type;

    protected Packet()
    {
    }

    @Override
    public final Type<Packet> type()
    {
        return type == null
                ? (type = new Type<>(createResourceLocation(Main.MODID, getPacketId())))
                : type;
    }

    public abstract boolean isServerToClient();

    public abstract void onReceive(Player player);
}
