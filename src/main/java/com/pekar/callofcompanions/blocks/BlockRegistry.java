package com.pekar.callofcompanions.blocks;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

import static com.pekar.callofcompanions.Main.MODID;
import static com.pekar.callofcompanions.utils.Resources.createResourceLocation;

public class BlockRegistry
{
    public static final TagKey<Block> CALL_CRYSTAL_NOT_FULL_USABLE_TAG = TagKey.create(Registries.BLOCK, createResourceLocation(MODID, "call_crystal_not_full_usable"));
}
