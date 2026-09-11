package com.pekar.callofcompanions.network;

import com.pekar.callofcompanions.ClientConfig;
import com.pekar.callofcompanions.network.base.IPacket;
import com.pekar.callofcompanions.network.base.ServerToClientPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class CompanionUpdatedPacket extends ServerToClientPacket
{
    public enum Cause { DISMOUNT, INTERACT }

    private final Cause cause;

    CompanionUpdatedPacket()
    {
        this(Cause.DISMOUNT);
    }

    public CompanionUpdatedPacket(Cause cause)
    {
        this.cause = cause;
    }

    @Override
    public void onReceive(Player player)
    {
        // Evaluate the receiving client's preference, independently of the data update.
        boolean showMessage = switch (cause)
        {
            case DISMOUNT -> ClientConfig.SHOW_UPDATE_MESSAGE_ON_DISMOUNT.get();
            case INTERACT -> ClientConfig.SHOW_UPDATE_MESSAGE_ON_INTERACT.get();
        };

        if (showMessage)
        {
            player.displayClientMessage(Component.translatable("message.callofcompanions.companion_updated"), true);
        }
    }

    @Override
    public void encode(FriendlyByteBuf buffer)
    {
        buffer.writeEnum(cause);
    }

    @Override
    public String getPacketId()
    {
        return Packets.CompanionUpdatedPacketId;
    }

    @Override
    public IPacket decode(FriendlyByteBuf buffer)
    {
        return new CompanionUpdatedPacket(buffer.readEnum(Cause.class));
    }
}
