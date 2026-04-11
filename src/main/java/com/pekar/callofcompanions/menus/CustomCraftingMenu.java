package com.pekar.callofcompanions.menus;

import com.pekar.callofcompanions.data.DataRegistry;
import com.pekar.callofcompanions.items.ItemRegistry;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;

import java.util.UUID;

public class CustomCraftingMenu extends CraftingMenu
{
    public CustomCraftingMenu(int containerId, Inventory playerInventory)
    {
        super(containerId, playerInventory);
    }

    public CustomCraftingMenu(int containerId, Inventory playerInventory, ContainerLevelAccess access)
    {
        super(containerId, playerInventory, access);
    }

    @Override
    public void slotsChanged(Container inventory)
    {
        super.slotsChanged(inventory);

        var result = resultSlots.getItem(0);

        if (!result.isEmpty() && result.is(ItemRegistry.DEEP_CALL_CRYSTAL))
        {
            for (int i = 0; i < inventory.getContainerSize(); i++)
            {
                var stack = inventory.getItem(i);
                if (stack.isEmpty()) continue;

                if (stack.is(ItemRegistry.CALL_CRYSTAL))
                {
                    var companionData = stack.get(DataRegistry.COMPANIONS);
                    if (companionData != null && !result.isEmpty())
                    {
                        result.set(DataRegistry.CRYSTAL_ID, UUID.randomUUID());
                        result.set(DataRegistry.COMPANIONS, companionData.copyWithCapacity(DataRegistry.DEEP_CRYSTAL_DATA_CAPACITY));

                        // write back the modified result stack
                        resultSlots.setItem(0, result);
                    }

                    break;
                }
            }
        }
    }
}
