package com.pekar.callofcompanions.mixin;

import com.pekar.callofcompanions.data.DataRegistry;
import com.pekar.callofcompanions.items.CallCrystal;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin
{
    @Shadow @Final private Minecraft minecraft;
    @Shadow private ItemStack mainHandItem;
    @Shadow private ItemStack offHandItem;

    @Inject(method = "tick", at = @At("HEAD"))
    private void callofcompanions$keepVisibleCallCrystal(CallbackInfo ci)
    {
        var player = minecraft.player;
        if (player == null) return;

        var currentMainHandItem = player.getMainHandItem();
        if (isSameCallCrystal(mainHandItem, currentMainHandItem))
            mainHandItem = currentMainHandItem;

        var currentOffHandItem = player.getOffhandItem();
        if (isSameCallCrystal(offHandItem, currentOffHandItem))
            offHandItem = currentOffHandItem;
    }

    private static boolean isSameCallCrystal(ItemStack oldStack, ItemStack newStack)
    {
        if (!(oldStack.getItem() instanceof CallCrystal) || !(newStack.getItem() instanceof CallCrystal)) return false;

        var oldCrystalId = oldStack.get(DataRegistry.CRYSTAL_ID);
        return oldCrystalId != null && oldCrystalId.equals(newStack.get(DataRegistry.CRYSTAL_ID));
    }
}
