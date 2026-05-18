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
import net.minecraft.world.entity.animal.Animal;
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
        if (!(event.getTarget() instanceof Animal animal)) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        boolean updated = updateAnimalPos(player, animal);
        if (updated && Config.SHOW_UPDATE_MESSAGE_ON_INTERACT.get())
        {
            player.sendOverlayMessage(Component.translatable("message.callofcompanions.companion_updated"));
        }
    }

    private boolean updateAnimalPos(ServerPlayer serverPlayer, Animal animal)
    {
        if (!(animal.level() instanceof ServerLevel animalLevel)) return false;

        boolean companionUpdated = false;
        for (var itemStack : serverPlayer.getInventory().getNonEquipmentItems())
        {
            if (!itemStack.is(ItemRegistry.CALL_CRYSTALS_TAG)) continue;
            var data = itemStack.get(DataRegistry.COMPANIONS);
            if (data == null) continue;
            var entry = data.getCompanion(animal.getUUID());
            if (entry == null) continue;

            var oldPos = entry.pos();
            int maxDistance = Config.AUTO_UPDATE_DISTANCE_THRESHOLD.get();
            if (entry.dimension().equals(animalLevel.dimension())
                    && animal.distanceToSqr(oldPos.getX(), oldPos.getY(), oldPos.getZ()) < (double) maxDistance * maxDistance)
            {
                continue;
            }

            CallCrystalHelper.updateCompanionPos(animalLevel, data, entry);
            itemStack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, false);
            saveStackChanges(serverPlayer, itemStack, itemStack.get(DataRegistry.CRYSTAL_ID), data);
            companionUpdated = true;
        }

        return companionUpdated;
    }

    private void saveStackChanges(ServerPlayer serverPlayer, ItemStack stack, UUID crystalId, CompanionData companionData)
    {
        LOGGER.debug("Saving call crystal companion data: player={}, crystalId={}, companionCount={}",
                serverPlayer.getName().getString(),
                crystalId,
                companionData.companions().size());
        var data = companionData.copy();
        stack.remove(DataRegistry.COMPANIONS);
        stack.set(DataRegistry.COMPANIONS, data);
        new SaveCompanionsPacket(crystalId, data).sendToPlayer(serverPlayer);
    }
}
