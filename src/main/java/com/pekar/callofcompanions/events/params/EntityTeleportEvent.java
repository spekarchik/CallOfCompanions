package com.pekar.callofcompanions.events.params;

import net.minecraft.world.entity.Entity;

public final class EntityTeleportEvent
{
    private final Entity entity;

    public EntityTeleportEvent(Entity entity)
    {
        this.entity = entity;
    }

    public Entity getEntity()
    {
        return entity;
    }
}

