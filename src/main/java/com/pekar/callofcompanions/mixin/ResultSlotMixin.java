package com.pekar.callofcompanions.mixin;

import com.pekar.callofcompanions.events.fabric.FabricCustomizationEventHooks;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ResultSlot.class)
public abstract class ResultSlotMixin
{
    @Shadow @Final private Container craftSlots;

    @Inject(method = "onTake", at = @At("HEAD"))
    private void callofcompanions$beforeOnTake(Player player, ItemStack stack, CallbackInfoReturnable<ItemStack> cir)
    {
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        FabricCustomizationEventHooks.onItemCrafted(serverPlayer, stack, craftSlots);
    }
}

