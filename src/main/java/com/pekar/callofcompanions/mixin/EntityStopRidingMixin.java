package com.pekar.callofcompanions.mixin;

import com.pekar.callofcompanions.events.fabric.FabricAnimalEventHooks;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityStopRidingMixin
{
    @Shadow
    public abstract Entity getVehicle();

    @Inject(method = "stopRiding", at = @At("HEAD"))
    private void callofcompanions$beforeStopRiding(CallbackInfo ci)
    {
        var self = (Entity) (Object) this;
        if (self.level().isClientSide()) return;

        var vehicle = getVehicle();
        if (vehicle == null) return;

        FabricAnimalEventHooks.onEntityDismount(self, vehicle);
    }
}

