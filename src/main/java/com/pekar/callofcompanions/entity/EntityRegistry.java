package com.pekar.callofcompanions.entity;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

import static com.pekar.callofcompanions.Main.MODID;
import static com.pekar.callofcompanions.utils.Resources.createResourceLocation;

public class EntityRegistry
{
    public static final TagKey<EntityType<?>> ANIMALS_CAN_TELEPORT_TO_PLAYER = TagKey.create(Registries.ENTITY_TYPE, createResourceLocation(MODID,"animals_can_teleport_to_player"));

}
