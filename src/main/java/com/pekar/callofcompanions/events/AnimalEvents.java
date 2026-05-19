package com.pekar.callofcompanions.events;

import com.mojang.logging.LogUtils;
import com.pekar.callofcompanions.Config;
import com.pekar.callofcompanions.controllers.CallCrystalHelper;
import com.pekar.callofcompanions.data.CompanionData;
import com.pekar.callofcompanions.data.DataRegistry;
import com.pekar.callofcompanions.items.ItemRegistry;
import com.pekar.callofcompanions.network.SaveCompanionsPacket;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityMountEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.slf4j.Logger;

import java.util.UUID;

public class AnimalEvents implements IEventHandler
{
    private static final Logger LOGGER = LogUtils.getLogger();

    @SubscribeEvent
    public void onEntityMount(EntityMountEvent event)
    {
        if (!Config.AUTO_UPDATE_ON_DISMOUNT.get()) return;
        if (event.isMounting()) return;
        if (!(event.getEntityMounting() instanceof ServerPlayer player)) return;
        if (!(event.getEntityBeingMounted() instanceof Animal animal)) return;
        if (!isCorrectAnimalForBinding(animal)) return;

        boolean updated = updateAnimalPos(player, animal);
        if (updated && Config.SHOW_UPDATE_MESSAGE_ON_DISMOUNT.get())
        {
            player.sendOverlayMessage(Component.translatable("message.callofcompanions.companion_updated"));
        }
    }

    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent.EntityInteract event)
    {
        if (!Config.AUTO_UPDATE_ON_INTERACT.get()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!(event.getTarget() instanceof Animal animal)) return;
        if (!isCorrectAnimalForBinding(animal)) return;
        if (player.getItemInHand(event.getHand()).is(ItemRegistry.CALL_CRYSTALS_TAG)) return;

        boolean updated = updateAnimalPos(player, animal);
        if (updated && Config.SHOW_UPDATE_MESSAGE_ON_INTERACT.get())
        {
            player.sendOverlayMessage(Component.translatable("message.callofcompanions.companion_updated"));
        }
    }

    private boolean isCorrectAnimalForBinding(Animal animal)
    {
        boolean isTameAnimal = animal instanceof TamableAnimal tamable && tamable.isTame();
        boolean isTamedHorse = animal instanceof AbstractHorse horse && horse.isTamed();
        return isTameAnimal || isTamedHorse || (Config.DEEP_CRYSTAL_ALLOW_UNTAMED.isTrue() && animal.hasCustomName());
    }

    private boolean updateAnimalPos(ServerPlayer serverPlayer, Animal animal)
    {
        if (!(animal.level() instanceof ServerLevel animalLevel)) return false;

        boolean companionUpdated = tryRefreshCrystalData(serverPlayer, animal, animalLevel, serverPlayer.getOffhandItem(), Inventory.SLOT_OFFHAND);

        var inventory = serverPlayer.getInventory();
        for (int slotIdx = 0; slotIdx < inventory.getContainerSize(); slotIdx++)
        {
            var itemStack = inventory.getItem(slotIdx);
            if (!tryRefreshCrystalData(serverPlayer, animal, animalLevel, itemStack, slotIdx))
                continue;

            companionUpdated = true;
        }

        return companionUpdated;
    }

    private boolean tryRefreshCrystalData(ServerPlayer serverPlayer, Animal animal, ServerLevel animalLevel, ItemStack itemStack, int slotIdx)
    {
        if (!itemStack.is(ItemRegistry.CALL_CRYSTALS_TAG)) return false;

        var data = itemStack.get(DataRegistry.COMPANIONS);
        if (data == null) return false;
        var entry = data.getCompanion(animal.getUUID());
        if (entry == null) return false;

        var oldPos = entry.pos();
        int maxDistance = Config.AUTO_UPDATE_DISTANCE_THRESHOLD.get();
        if (entry.dimension().equals(animalLevel.dimension())
                && animal.distanceToSqr(oldPos.getX(), oldPos.getY(), oldPos.getZ()) < (double) maxDistance * maxDistance)
        {
            return false;
        }

        CallCrystalHelper.updateCompanionPos(animalLevel, data, entry);
        itemStack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, false);
        saveStackChanges(serverPlayer, itemStack, itemStack.get(DataRegistry.CRYSTAL_ID), data, slotIdx);
        return true;
    }

    private void saveStackChanges(ServerPlayer serverPlayer, ItemStack stack, UUID crystalId, CompanionData companionData, int slotIndex)
    {
        LOGGER.debug("Saving call crystal companion data: player={}, crystalId={}, companionCount={}",
                serverPlayer.getName().getString(),
                crystalId,
                companionData.companions().size());

        var data = companionData.copy();
        stack.set(DataRegistry.COMPANIONS, data);
        new SaveCompanionsPacket(crystalId, slotIndex, data).sendToPlayer(serverPlayer);
    }
}
