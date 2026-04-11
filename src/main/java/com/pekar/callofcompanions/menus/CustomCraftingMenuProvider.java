package com.pekar.callofcompanions.menus;

import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import org.jetbrains.annotations.Nullable;

public class CustomCraftingMenuProvider implements MenuProvider
{
    private final ContainerLevelAccess containerLevelAccess;

    public CustomCraftingMenuProvider(ContainerLevelAccess containerLevelAccess)
    {
        this.containerLevelAccess = containerLevelAccess;
    }

    @Override
    public Component getDisplayName()
    {
        return Component.translatable("container.crafting");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int id, Inventory inventory, Player player)
    {
        return new CustomCraftingMenu(id, inventory, containerLevelAccess);
    }
}
