package com.pekar.callofcompanions.events;

import com.mojang.logging.LogUtils;
import com.pekar.callofcompanions.events.params.ServerStoppedEvent;
import com.pekar.callofcompanions.controllers.config.TrackingPreferencesController;
import com.pekar.callofcompanions.scheduler.CompanionEntryScheduler;
import org.slf4j.Logger;

public class WorldEvents implements IEventHandler
{
    public static final Logger LOGGER = LogUtils.getLogger();

    public void onServerStopped(ServerStoppedEvent event)
    {
        CompanionEntryScheduler.DELAY_TASKS.clear();
        CompanionEntryScheduler.TELEPORT_TASKS.clear();
        CompanionEntryScheduler.UPDATE_POS_TASKS.clear();
        LOGGER.debug("Tasks cleared.");

        TrackingPreferencesController.clear();
    }
}
