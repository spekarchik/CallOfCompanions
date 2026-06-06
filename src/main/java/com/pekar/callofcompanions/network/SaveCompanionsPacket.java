package com.pekar.callofcompanions.network;

import com.pekar.callofcompanions.controllers.CallCrystalHelper;
import com.pekar.callofcompanions.data.CompanionData;
import com.pekar.callofcompanions.data.DataRegistry;
import com.pekar.callofcompanions.network.base.IPacket;
import com.pekar.callofcompanions.network.base.ServerToClientPacket;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public class SaveCompanionsPacket extends ServerToClientPacket
{
    private final UUID crystalId;
    private final CompanionData companionData;
    private final int slotId;

    SaveCompanionsPacket()
    {
        this(null, Inventory.NOT_FOUND_INDEX, null);
    }

    public SaveCompanionsPacket(UUID crystalId, int slotId, CompanionData companionData)
    {
        this.crystalId = crystalId;
        this.companionData = companionData;
        this.slotId = slotId;
    }

    @Override
    public void onReceive(LocalPlayer player)
    {
        if (slotId == Inventory.SLOT_OFFHAND)
        {
            var offHandItem = player.getOffhandItem();
            if (CallCrystalHelper.hasSameId(offHandItem, crystalId))
            {
                offHandItem.set(DataRegistry.COMPANIONS, companionData);
                offHandItem.set(DataComponents.MAX_STACK_SIZE, 1);
                return;
            }
        }
        else if (slotId >= 0)
        {
            var slot = player.getSlot(slotId);
            var slotItem = slot != null ? slot.get() : ItemStack.EMPTY;
            if (CallCrystalHelper.hasSameId(slotItem, crystalId))
            {
                slotItem.set(DataRegistry.COMPANIONS, companionData);
                slotItem.set(DataComponents.MAX_STACK_SIZE, 1);
                return;
            }
        }

        for (var itemStack : player.getInventory().getNonEquipmentItems())
        {
            if (!CallCrystalHelper.hasSameId(itemStack, crystalId)) continue;

            itemStack.set(DataRegistry.COMPANIONS, companionData);
            itemStack.set(DataComponents.MAX_STACK_SIZE, 1);
            break;
        }
    }

    @Override
    public void encode(FriendlyByteBuf buffer)
    {
        buffer.writeUUID(crystalId);
        buffer.writeInt(slotId);
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
        var crystalId = buffer.readUUID();
        var slotId = buffer.readInt();
        var dataTag = buffer.readNbt();
        var data = CompanionData.CODEC.parse(NbtOps.INSTANCE, dataTag).getOrThrow();
        return new SaveCompanionsPacket(crystalId, slotId, data);
    }
}
