package com.pekar.callofcompanions.events.params;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Iterator;
import java.util.NoSuchElementException;

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

    public static final class ItemCraftedEvent extends PlayerEventBase
    {
        private final ItemStack crafting;
        private final Container inventory;

        public ItemCraftedEvent(Player entity, ItemStack crafting, Container inventory)
        {
            super(entity);
            this.crafting = crafting;
            this.inventory = inventory;
        }

        public ItemStack getCrafting()
        {
            return crafting;
        }

        public Iterable<ItemStack> getInventory()
        {
            return new ContainerView(inventory);
        }
    }

    private static final class ContainerView implements Iterable<ItemStack>
    {
        private final Container container;

        private ContainerView(Container container)
        {
            this.container = container;
        }

        @Override
        public Iterator<ItemStack> iterator()
        {
            return new Iterator<>()
            {
                private int idx = 0;

                @Override
                public boolean hasNext()
                {
                    return idx < container.getContainerSize();
                }

                @Override
                public ItemStack next()
                {
                    if (!hasNext()) throw new NoSuchElementException();
                    return container.getItem(idx++);
                }
            };
        }
    }
}
