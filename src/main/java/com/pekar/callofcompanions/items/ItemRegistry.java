package com.pekar.callofcompanions.items;

import com.pekar.callofcompanions.Main;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;

public class ItemRegistry
{
    public static final DeferredItem<Item> CALL_CRYSTAL = Main.ITEMS.registerItem("call_crystal", CallCrystal::new, p -> p.stacksTo(1));

    public static void initStatic()
    {
        // just to initialize static members
    }
}
