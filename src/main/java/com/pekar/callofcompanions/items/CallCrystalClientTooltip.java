package com.pekar.callofcompanions.items;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;

@Environment(EnvType.CLIENT)
final class CallCrystalClientTooltip
{
    private CallCrystalClientTooltip()
    {
    }

    static Level getLevel()
    {
        return Minecraft.getInstance().level;
    }

    static boolean hasShiftDown()
    {
        var window = Minecraft.getInstance().getWindow().getWindow();
        return InputConstants.isKeyDown(window, InputConstants.KEY_LSHIFT)
                || InputConstants.isKeyDown(window, InputConstants.KEY_RSHIFT);
    }

    static boolean hasAltDown()
    {
        var window = Minecraft.getInstance().getWindow().getWindow();
        return InputConstants.isKeyDown(window, InputConstants.KEY_LALT)
                || InputConstants.isKeyDown(window, InputConstants.KEY_RALT);
    }
}
