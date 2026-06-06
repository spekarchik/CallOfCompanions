package com.pekar.callofcompanions.items;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

import java.util.function.Function;

import static com.pekar.callofcompanions.Main.MODID;
import static com.pekar.callofcompanions.utils.Resources.createResourceLocation;

public class ItemRegistry
{
    public static final TagKey<Item> CALL_CRYSTALS_TAG = TagKey.create(Registries.ITEM, createResourceLocation(MODID, "call_crystals"));

    public static final Item CALL_CRYSTAL = registerItem("call_crystal", properties -> new CallCrystal(properties.rarity(Rarity.UNCOMMON)));
    public static final Item DEEP_CALL_CRYSTAL = registerItem("deep_call_crystal", properties -> new DeepCallCrystal(properties.rarity(Rarity.RARE)));

    public static void initStatic()
    {
        // just to initialize static members
    }

    private static Item registerItem(String name, Function<Item.Properties, Item> itemFactory)
    {
        var id = createResourceLocation(MODID, name);
        var key = ResourceKey.create(Registries.ITEM, id);
        var item = itemFactory.apply(new Item.Properties().setId(key));
        return Registry.register(BuiltInRegistries.ITEM, id, item);
    }
}
