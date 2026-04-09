package com.pekar.callofcompanions.events;

import com.mojang.logging.LogUtils;
import com.pekar.callofcompanions.scheduler.CompanionEntryScheduler;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import org.slf4j.Logger;

public class WorldEvents implements IEventHandler
{
    public static final Logger LOGGER = LogUtils.getLogger();

    @SubscribeEvent
    public void onServerStoppingEvent(ServerStoppingEvent event)
    {
        CompanionEntryScheduler.DELAY_TASKS.clear();
        CompanionEntryScheduler.TELEPORT_TASKS.clear();
        CompanionEntryScheduler.UPDATE_POS_TASKS.clear();
        LOGGER.debug("UuidScheduledTask cleared.");
    }
}
