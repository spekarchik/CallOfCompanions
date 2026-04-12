package com.pekar.callofcompanions.network;

import com.pekar.callofcompanions.controllers.CallCrystalHelper;
import com.pekar.callofcompanions.data.CompanionData;
import com.pekar.callofcompanions.data.DataRegistry;
import com.pekar.callofcompanions.network.base.IPacket;
import com.pekar.callofcompanions.network.base.ServerToClientPacket;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public class SaveCompanionsPacket extends ServerToClientPacket
{
    private final UUID crystalId;
    private final CompanionData companionData;

    public SaveCompanionsPacket()
    {
        this(null, null);
    }

    public SaveCompanionsPacket(UUID crystalId, CompanionData companionData)
    {
        this.crystalId = crystalId;
        this.companionData = companionData;
    }

    @Override
    public void onReceive(IPayloadContext context)
    {
        var player = context.player();
        for (var itemStack : player.getInventory().getNonEquipmentItems())
        {
            if (!CallCrystalHelper.hasSameId(itemStack, crystalId)) continue;

            itemStack.remove(DataRegistry.CRYSTAL_ID);
            itemStack.set(DataRegistry.CRYSTAL_ID, crystalId);
            itemStack.remove(DataRegistry.COMPANIONS);
            itemStack.set(DataRegistry.COMPANIONS, companionData);
            break;
        }
    }

    @Override
    public void encode(FriendlyByteBuf buffer)
    {
        buffer.writeUUID(crystalId);
        var data = CompanionData.CODEC.encodeStart(NbtOps.INSTANCE, companionData).getOrThrow();
        buffer.writeNbt(data);
    }

    @Override
    public String getPacketId()
    {
        return Packets.SaveCompanionsPacketId;
    }

    @Override
    public IPacket decode(FriendlyByteBuf buffer)
    {
        var id = buffer.readUUID();
        var dataTag = buffer.readNbt();
        var data = CompanionData.CODEC.parse(NbtOps.INSTANCE, dataTag).getOrThrow();
        return new SaveCompanionsPacket(id, data);
    }
}
