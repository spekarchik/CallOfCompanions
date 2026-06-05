package com.pekar.callofcompanions.scheduler;

import net.minecraft.server.level.ServerPlayer;

import java.util.*;

public class CompanionEntryScheduler
{
    private final Set<CompanionEntryTask> tasks = new HashSet<>();
    private final List<CompanionEntryTask> pendingAdds = new ArrayList<>();

    public static final CompanionEntryScheduler DELAY_TASKS = new CompanionEntryScheduler();
    public static final CompanionEntryScheduler TELEPORT_TASKS = new CompanionEntryScheduler();
    public static final CompanionEntryScheduler UPDATE_POS_TASKS = new CompanionEntryScheduler();
    private static final Dictionary<UUID, Integer> playerTaskCounters = new Hashtable<>();
    private static final Dictionary<UUID, TaskEndListener> playerTaskEndListeners = new Hashtable<>();

    private CompanionEntryScheduler()
    {}

    public static void listen(ServerPlayer player, TaskEndListener listener)
    {
        playerTaskEndListeners.put(player.getUUID(), listener);
    }

    public static boolean hasTasks(ServerPlayer player)
    {
        return playerTaskCounters.get(player.getUUID()) != null;
    }

    public void add(CompanionEntryTask task)
    {
        pendingAdds.add(task);
    }

    public void clear()
    {
        var iterator = tasks.iterator();
        while (iterator.hasNext())
        {
            var task = iterator.next();
            task.cancel();
            removeTask(task, iterator);
        }
    }

    public void clearFor(ServerPlayer player)
    {
        var iterator = tasks.iterator();
        while (iterator.hasNext())
        {
            var task = iterator.next();
            if (!task.initiator().getUUID().equals(player.getUUID())) continue;

            task.cancel();
            removeTask(task, iterator);
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
                removeTask(task, iterator);
            }
        }

        for (var task : pendingAdds)
        {
            if (tasks.add(task))
                incrementTaskCount(task.initiator().getUUID());
        }
        pendingAdds.clear();
    }

    private int taskCount(UUID uuid)
    {
        var taskCount = playerTaskCounters.get(uuid);
        return taskCount != null ? taskCount : 0;
    }

    private void incrementTaskCount(UUID uuid)
    {
        int count = taskCount(uuid);
        playerTaskCounters.put(uuid, count + 1);
    }

    private void decrementTaskCount(UUID uuid)
    {
        int count = taskCount(uuid);
        if (count == 1)
        {
            playerTaskCounters.remove(uuid);
            var listener = playerTaskEndListeners.get(uuid);
            if (listener != null)
            {
                listener.onAllTasksEnd();
                playerTaskEndListeners.remove(uuid);
            }
        }
        else
        {
            playerTaskCounters.put(uuid, count - 1);
        }
    }

    private void removeTask(CompanionEntryTask task, Iterator<CompanionEntryTask> iterator)
    {
        iterator.remove();
        decrementTaskCount(task.initiator().getUUID());
    }
}
