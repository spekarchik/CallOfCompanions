package com.pekar.callofcompanions.events.params;

import net.minecraft.world.entity.Entity;

public final class EntityMountEvent
{
    private final boolean mounting;
    private final Entity entityMounting;
    private final Entity entityBeingMounted;

    public EntityMountEvent(boolean mounting, Entity entityMounting, Entity entityBeingMounted)
    {
        this.mounting = mounting;
        this.entityMounting = entityMounting;
        this.entityBeingMounted = entityBeingMounted;
    }

    public boolean isMounting()
    {
        return mounting;
    }

    public Entity getEntityMounting()
    {
        return entityMounting;
    }

    public Entity getEntityBeingMounted()
    {
        return entityBeingMounted;
    }
}

