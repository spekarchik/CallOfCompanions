package com.pekar.callofcompanions.events.params;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

public final class PlayerInteractEvent
{
    private PlayerInteractEvent()
    {}

    public static final class EntityInteractSpecific
    {
        private final Player entity;
        private final Level level;
        private final InteractionHand hand;
        private final Entity target;
        private final ItemStack itemStack;
        private final EntityHitResult hitResult;

        private boolean canceled;
        private InteractionResult cancellationResult = InteractionResult.PASS;

        public EntityInteractSpecific(Player entity, Level level, InteractionHand hand, Entity target, ItemStack itemStack, EntityHitResult hitResult)
        {
            this.entity = entity;
            this.level = level;
            this.hand = hand;
            this.target = target;
            this.itemStack = itemStack;
            this.hitResult = hitResult;
        }

        public Player getEntity()
        {
            return entity;
        }

        public Level getLevel()
        {
            return level;
        }

        public InteractionHand getHand()
        {
            return hand;
        }

        public Entity getTarget()
        {
            return target;
        }

        public ItemStack getItemStack()
        {
            return itemStack;
        }

        public EntityHitResult getHitResult()
        {
            return hitResult;
        }

        public void setCanceled(boolean canceled)
        {
            this.canceled = canceled;
        }

        public boolean isCanceled()
        {
            return canceled;
        }

        public void setCancellationResult(InteractionResult result)
        {
            this.cancellationResult = result;
        }

        public InteractionResult getCancellationResult()
        {
            return cancellationResult;
        }
    }
}

