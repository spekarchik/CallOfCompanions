package com.pekar.callofcompanions.events;

import com.pekar.callofcompanions.scheduler.CompanionEntryScheduler;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

public class TickEvents implements IEventHandler
{
    @SubscribeEvent
    public void onServerTickEvent(ServerTickEvent.Post event)
    {
        CompanionEntryScheduler.DELAY_TASKS.tick();
        CompanionEntryScheduler.TELEPORT_TASKS.tick();
        CompanionEntryScheduler.UPDATE_POS_TASKS.tick();
    }
}
