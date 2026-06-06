package com.pekar.callofcompanions.mixin;

import com.pekar.callofcompanions.events.fabric.FabricPlayerEventHooks;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerTeleportMixin
{
    @Inject(method = "teleportTo", at = @At("HEAD"))
    private void callofcompanions$beforeTeleportTo(ServerLevel level, double x, double y, double z, float yRot, float xRot, CallbackInfo ci)
    {
        FabricPlayerEventHooks.onPlayerTeleport((ServerPlayer) (Object) this);
    }
}
