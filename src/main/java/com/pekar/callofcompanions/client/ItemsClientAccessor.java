package com.pekar.callofcompanions.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.pekar.callofcompanions.clientaccess.IItemsClientAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;

public class ItemsClientAccessor implements IItemsClientAccessor
{
    @Override
    public Level getLevel()
    {
        return Minecraft.getInstance().level;
    }

    @Override
    public boolean hasShiftDown()
    {
        var window = Minecraft.getInstance().getWindow();
        return InputConstants.isKeyDown(window, InputConstants.KEY_LSHIFT)
                || InputConstants.isKeyDown(window, InputConstants.KEY_RSHIFT);
    }

    @Override
    public boolean hasAltDown()
    {
        var window = Minecraft.getInstance().getWindow();
        return InputConstants.isKeyDown(window, InputConstants.KEY_LALT)
                || InputConstants.isKeyDown(window, InputConstants.KEY_RALT);
    }
}
