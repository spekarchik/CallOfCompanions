package com.pekar.callofcompanions.events;

import com.pekar.callofcompanions.events.params.ServerTickEvent;
import com.pekar.callofcompanions.scheduler.CompanionEntryScheduler;

public class TickEvents implements IEventHandler
{
    public void onServerTickEvent(ServerTickEvent.Post event)
    {
        CompanionEntryScheduler.DELAY_TASKS.tick();
        CompanionEntryScheduler.TELEPORT_TASKS.tick();
        CompanionEntryScheduler.UPDATE_POS_TASKS.tick();
    }
}
