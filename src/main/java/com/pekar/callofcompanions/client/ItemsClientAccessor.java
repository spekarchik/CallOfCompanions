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
        return InputConstants.isKeyDown(InputConstants.KEY_LSHIFT)
                || InputConstants.isKeyDown(InputConstants.KEY_RSHIFT);
    }

    @Override
    public boolean hasAltDown()
    {
        return InputConstants.isKeyDown(InputConstants.KEY_LALT)
                || InputConstants.isKeyDown(InputConstants.KEY_RALT);
    }
}
