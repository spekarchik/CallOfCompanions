package com.pekar.callofcompanions.items;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

import static com.pekar.callofcompanions.Main.MODID;
import static com.pekar.callofcompanions.utils.Resources.createResourceLocation;

public class ItemRegistry
{
    public static final TagKey<Item> CALL_CRYSTALS_TAG = TagKey.create(Registries.ITEM, createResourceLocation(MODID, "call_crystals"));

    public static final Item CALL_CRYSTAL = registerItem("call_crystal", new CallCrystal(new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final Item DEEP_CALL_CRYSTAL = registerItem("deep_call_crystal", new DeepCallCrystal(new Item.Properties().rarity(Rarity.RARE)));

    public static void initStatic()
    {
        // just to initialize static members
    }

    private static Item registerItem(String name, Item item)
    {
        return Registry.register(BuiltInRegistries.ITEM, createResourceLocation(MODID, name), item);
    }
}
