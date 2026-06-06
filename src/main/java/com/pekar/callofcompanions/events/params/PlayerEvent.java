package com.pekar.callofcompanions.events.params;

import net.minecraft.world.entity.player.Player;

public final class PlayerEvent
{
    private PlayerEvent()
    {}

    public abstract static class PlayerEventBase
    {
        private final Player entity;

        protected PlayerEventBase(Player entity)
        {
            this.entity = entity;
        }

        public Player getEntity()
        {
            return entity;
        }
    }

    public static final class PlayerLoggedOutEvent extends PlayerEventBase
    {
        public PlayerLoggedOutEvent(Player entity)
        {
            super(entity);
        }
    }

    public static final class PlayerChangedDimensionEvent extends PlayerEventBase
    {
        public PlayerChangedDimensionEvent(Player entity)
        {
            super(entity);
        }
    }

    public static final class PlayerRespawnEvent extends PlayerEventBase
    {
        public PlayerRespawnEvent(Player entity)
        {
            super(entity);
        }
    }
}

