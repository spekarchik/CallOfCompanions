package com.pekar.callofcompanions.events;

import com.pekar.callofcompanions.scheduler.UuidScheduledTask;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public class TickEvents implements IEventHandler
{
    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent.Post event)
    {
        UuidScheduledTask.tick();
    }
}
