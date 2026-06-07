package com.pekar.callofcompanions.mixin;

import com.pekar.callofcompanions.data.DataRegistry;
import com.pekar.callofcompanions.items.CallCrystal;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin
{
    @Inject(method = "shouldInstantlyReplaceVisibleItem", at = @At("HEAD"), cancellable = true)
    private void callofcompanions$replaceVisibleCallCrystal(ItemStack oldStack, ItemStack newStack, CallbackInfoReturnable<Boolean> cir)
    {
        if (!(oldStack.getItem() instanceof CallCrystal) || !(newStack.getItem() instanceof CallCrystal)) return;

        var oldCrystalId = oldStack.get(DataRegistry.CRYSTAL_ID);
        if (oldCrystalId == null || !oldCrystalId.equals(newStack.get(DataRegistry.CRYSTAL_ID))) return;

        cir.setReturnValue(true);
    }
}
