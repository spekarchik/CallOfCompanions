package com.pekar.callofcompanions.events;

import com.pekar.callofcompanions.Config;
import com.pekar.callofcompanions.data.DataRegistry;
import com.pekar.callofcompanions.items.ItemRegistry;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.UUID;

public class CustomizationEvents implements IEventHandler
{
    @SubscribeEvent
    public void onItemCraftedEvent(PlayerEvent.ItemCraftedEvent event)
    {
        var result = event.getCrafting();

        if (result.is(ItemRegistry.DEEP_CALL_CRYSTAL) && result.get(DataRegistry.COMPANIONS) == null)
        {
            var inventory = event.getInventory();
            for (int i = 0; i < inventory.getContainerSize(); i++)
            {
                var stack = event.getInventory().getItem(i);
                if (stack.is(ItemRegistry.CALL_CRYSTAL))
                {
                    var companionData = stack.get(DataRegistry.COMPANIONS);
                    if (companionData != null)
                    {
                        result.set(DataRegistry.CRYSTAL_ID, UUID.randomUUID());
                        result.set(DataRegistry.COMPANIONS,
                                companionData.copyWithCapacity((short) Config.DEEP_CRYSTAL_DATA_CAPACITY.getAsInt()));
                    }
                    break;
                }
            }
        }
    }
}
