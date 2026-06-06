package com.pekar.callofcompanions.events;

import com.pekar.callofcompanions.Config;
import com.pekar.callofcompanions.data.DataRegistry;
import com.pekar.callofcompanions.events.params.PlayerEvent;
import com.pekar.callofcompanions.items.ItemRegistry;
import net.minecraft.core.component.DataComponents;

import java.util.UUID;

public class CustomizationEvents implements IEventHandler
{
    public void onItemCraftedEvent(PlayerEvent.ItemCraftedEvent event)
    {
        var result = event.getCrafting();

        if (result.is(ItemRegistry.DEEP_CALL_CRYSTAL) && result.get(DataRegistry.COMPANIONS) == null)
        {
            for (var stack : event.getInventory())
            {
                if (stack.is(ItemRegistry.CALL_CRYSTAL))
                {
                    var companionData = stack.get(DataRegistry.COMPANIONS);
                    if (companionData != null)
                    {
                        result.set(DataRegistry.CRYSTAL_ID, UUID.randomUUID());
                        result.set(DataComponents.MAX_STACK_SIZE, 1);
                        result.set(DataRegistry.COMPANIONS,
                                companionData.copyWithCapacity((short) Config.DEEP_CRYSTAL_DATA_CAPACITY.getAsInt()));
                    }
                    break;
                }
            }
        }
    }
}
