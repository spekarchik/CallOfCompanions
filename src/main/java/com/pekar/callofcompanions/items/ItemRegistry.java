package com.pekar.callofcompanions.items;

import com.pekar.callofcompanions.Main;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredItem;

import static com.pekar.callofcompanions.Main.MODID;
import static com.pekar.callofcompanions.utils.Resources.createResourceLocation;

@SuppressWarnings({"removal"})
public class ItemRegistry
{
    public static final TagKey<Item> CALL_CRYSTALS_TAG = TagKey.create(Registries.ITEM, createResourceLocation(MODID, "call_crystals"));

    public static final DeferredItem<Item> CALL_CRYSTAL = Main.ITEMS.registerItem("call_crystal", CallCrystal::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> DEEP_CALL_CRYSTAL = Main.ITEMS.registerItem("deep_call_crystal", DeepCallCrystal::new, new Item.Properties().stacksTo(1).rarity(Rarity.RARE));

    public static void initStatic()
    {
        // just to initialize static members
    }
}
