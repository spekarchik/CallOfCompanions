package com.pekar.callofcompanions.events;

import com.pekar.callofcompanions.data.CompanionData;
import com.pekar.callofcompanions.data.CompanionEntry;
import com.pekar.callofcompanions.data.DataRegistry;
import com.pekar.callofcompanions.items.ItemRegistry;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public class PlayerInteractionEvents implements IEventHandler
{
    @SubscribeEvent
    public void onLivingInteractionEvent(PlayerInteractEvent.EntityInteractSpecific event)
    {
        var target = event.getTarget();
        if (target instanceof Player) return;

        var itemStack = event.getItemStack();
        var player = event.getEntity();

        if (itemStack.is(ItemRegistry.CALL_CRYSTAL))
        {
            boolean isTameAnimal = target instanceof TamableAnimal tamable && tamable.isTame();
            boolean isTamedHorse = target instanceof AbstractHorse horse && horse.isTamed();

            if (target instanceof Animal animal && (isTameAnimal || isTamedHorse))
            {
                var companions = itemStack.getOrDefault(DataRegistry.COMPANIONS, new CompanionData());

                var name = target.getDisplayName().getString();
                var companionType = target.getType().getDescription().getString();
                var entry = new CompanionEntry(target.getUUID(), name, companionType, player.getUUID(), player.getDisplayName().getString());
                companions.add(entry);
                itemStack.remove(DataRegistry.COMPANIONS);
                itemStack.set(DataRegistry.COMPANIONS, new CompanionData(companions.getCompanions()));

                event.setCanceled(true);
                event.setCancellationResult(event.getSide() == LogicalSide.CLIENT ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER);
            }
        }
    }
}
