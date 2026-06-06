package com.pekar.callofcompanions.events.params;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public final class EntityInteract
{
    private final Player entity;
    private final Entity target;
    private final InteractionHand hand;

    public EntityInteract(Player entity, Entity target, InteractionHand hand)
    {
        this.entity = entity;
        this.target = target;
        this.hand = hand;
    }

    public Player getEntity()
    {
        return entity;
    }

    public Entity getTarget()
    {
        return target;
    }

    public InteractionHand getHand()
    {
        return hand;
    }
}

