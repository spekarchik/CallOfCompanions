package com.pekar.callofcompanions.events;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class EntityInteractSpecific
{
    private final Player player;
    private final Level level;
    private final Entity target;
    private final ItemStack itemStack;
    private final LogicalSide side;

    private boolean canceled = false;
    private InteractionResult cancellationResult = InteractionResult.PASS;

    public EntityInteractSpecific(Player player, Level level, Entity target, ItemStack itemStack, LogicalSide side)
    {
        this.player = player;
        this.level = level;
        this.target = target;
        this.itemStack = itemStack;
        this.side = side;
    }

    public static EntityInteractSpecific fromFabric(Player player, Level level, InteractionHand hand, Entity target)
    {
        var side = level.isClientSide ? LogicalSide.CLIENT : LogicalSide.SERVER;
        return new EntityInteractSpecific(player, level, target, player.getItemInHand(hand), side);
    }

    public Player getEntity()
    {
        return player;
    }

    public Level getLevel()
    {
        return level;
    }

    public Entity getTarget()
    {
        return target;
    }

    public ItemStack getItemStack()
    {
        return itemStack;
    }

    public LogicalSide getSide()
    {
        return side;
    }

    public void setCanceled(boolean canceled)
    {
        this.canceled = canceled;
    }

    public boolean isCanceled()
    {
        return canceled;
    }

    public void setCancellationResult(InteractionResult cancellationResult)
    {
        this.cancellationResult = cancellationResult;
    }

    public InteractionResult getCancellationResult()
    {
        return cancellationResult;
    }
}

