package com.pekar.callofcompanions.scheduler;

import net.minecraft.server.level.ServerPlayer;

import java.util.HashSet;
import java.util.Set;

public class CompanionEntryScheduler
{
    private final Set<CompanionEntryTask> tasks = new HashSet<>();

    public static final CompanionEntryScheduler DELAY_TASKS = new CompanionEntryScheduler();
    public static final CompanionEntryScheduler TELEPORT_TASKS = new CompanionEntryScheduler();
    public static final CompanionEntryScheduler UPDATE_POS_TASKS = new CompanionEntryScheduler();

    private CompanionEntryScheduler()
    {}

    public void add(CompanionEntryTask task)
    {
        tasks.add(task);
    }

    public void clear()
    {
        var iterator = tasks.iterator();
        while (iterator.hasNext())
        {
            var task = iterator.next();
            task.cancel();
            if (task.isCompleted())
            {
                iterator.remove();
            }
        }
    }

    public void clearFor(ServerPlayer player)
    {
        var iterator = tasks.iterator();
        while (iterator.hasNext())
        {
            var task = iterator.next();
            if (!task.getCompanionEntry().ownerUuid().equals(player.getUUID())) continue;

            task.cancel();
            if (task.isCompleted())
            {
                iterator.remove();
            }
        }
    }

    public void tick()
    {
        var iterator = tasks.iterator();
        while (iterator.hasNext())
        {
            var task = iterator.next();
            task.decrementOrExecute();
            if (task.isCompleted())
            {
                iterator.remove();
            }
        }
    }
}
