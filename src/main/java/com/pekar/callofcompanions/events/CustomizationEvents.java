package com.pekar.callofcompanions.events;

import com.pekar.callofcompanions.data.DataRegistry;
import com.pekar.callofcompanions.items.ItemRegistry;
import com.pekar.callofcompanions.menus.CustomCraftingMenuProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.UUID;

public class CustomizationEvents implements IEventHandler
{
    @SubscribeEvent
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event)
    {
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        Player player = event.getEntity();
        BlockState state = level.getBlockState(pos);

        if (state.getBlock() == Blocks.CRAFTING_TABLE)
        {
            event.setCanceled(true); // Not the standard menu to be shown
            event.setCancellationResult(level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER);

            if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer)
            {
                var accessLevel = ContainerLevelAccess.create(level, pos);
                serverPlayer.openMenu(new CustomCraftingMenuProvider(accessLevel));
            }
        }
    }

    @SubscribeEvent
    public void onItemCraftedEvent(PlayerEvent.ItemCraftedEvent event)
    {
        var result = event.getCrafting();

        if (result.is(ItemRegistry.DEEP_CALL_CRYSTAL) && result.get(DataRegistry.COMPANIONS) == null)
        {
            for (var stack : event.getInventory())
            {
                if (stack.is(ItemRegistry.CALL_CRYSTAL))
                {
                    var companionData = stack.get(DataRegistry.COMPANIONS);
                    if (companionData != null)
                    {
                        result.set(DataRegistry.CRYSTAL_ID, UUID.randomUUID());
                        result.set(DataRegistry.COMPANIONS,
                                companionData.copyWithCapacity(DataRegistry.DEEP_CRYSTAL_DATA_CAPACITY));
                    }
                    break;
                }
            }
        }
    }
}
