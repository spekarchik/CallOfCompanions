package com.pekar.callofcompanions.items;

import com.pekar.callofcompanions.Main;
import com.pekar.callofcompanions.utils.Resources;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;

import static com.pekar.callofcompanions.Main.MODID;
import static com.pekar.callofcompanions.utils.Resources.createResourceLocation;

public class ItemRegistry
{
    public static final TagKey<Item> CALL_CRYSTALS_TAG = TagKey.create(Registries.ITEM, createResourceLocation(MODID, "call_crystals"));

    public static final DeferredItem<Item> CALL_CRYSTAL = Main.ITEMS.registerItem("call_crystal", CallCrystal::new, p -> p.stacksTo(1));
    public static final DeferredItem<Item> DEEP_CALL_CRYSTAL = Main.ITEMS.registerItem("deep_call_crystal", DeepCallCrystal::new, p -> p.stacksTo(1));

    public static void initStatic()
    {
        // just to initialize static members
    }
}
