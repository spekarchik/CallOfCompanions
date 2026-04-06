package com.pekar.callofcompanions.network;

import com.pekar.callofcompanions.data.CompanionData;
import com.pekar.callofcompanions.data.DataRegistry;
import com.pekar.callofcompanions.items.ItemRegistry;
import com.pekar.callofcompanions.network.base.IPacket;
import com.pekar.callofcompanions.network.base.ServerToClientPacket;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class SaveCompanionsPacket extends ServerToClientPacket
{
    private final CompanionData companionData;

    public SaveCompanionsPacket()
    {
        this(null);
    }

    public SaveCompanionsPacket(CompanionData companionData)
    {
        this.companionData = companionData;
    }

    @Override
    public void onReceive(IPayloadContext context)
    {
        var player = context.player();
        ItemStack stack;
        var mainHandItem = player.getMainHandItem();
        if (mainHandItem.is(ItemRegistry.CALL_CRYSTAL))
        {
            stack = mainHandItem;
        }
        else if (player.getOffhandItem().is(ItemRegistry.CALL_CRYSTAL))
        {
            stack = player.getOffhandItem();
        }
        else
        {
            return;
        }

        if (stack.has(DataRegistry.COMPANIONS))
        {
            stack.remove(DataRegistry.COMPANIONS);
            stack.set(DataRegistry.COMPANIONS, companionData);
        }
    }

    @Override
    public void encode(FriendlyByteBuf buffer)
    {
        var tag = CompanionData.CODEC.encodeStart(NbtOps.INSTANCE, companionData).getOrThrow();
        buffer.writeNbt(tag);
    }

    @Override
    public String getPacketId()
    {
        return Packets.SaveCompanionsPacketId;
    }

    @Override
    public IPacket decode(FriendlyByteBuf buffer)
    {
        var tag = buffer.readNbt();
        var data = CompanionData.CODEC.parse(NbtOps.INSTANCE, tag).getOrThrow();
        return new SaveCompanionsPacket(data);
    }
}
