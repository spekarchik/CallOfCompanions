package com.pekar.callofcompanions.scheduler;

import com.pekar.callofcompanions.scheduler.base.ScheduledTask;

import java.util.Set;
import java.util.HashSet;
import java.util.UUID;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public class UuidScheduledTask extends ScheduledTask<UUID>
{
    private static final Set<UuidScheduledTask> tasks = new HashSet<>();

    public UuidScheduledTask(int ticks, UUID object, Function<UUID, Boolean> doOnTick, Consumer<UUID> doOnComplete, Consumer<UUID> doOnCancel)
    {
        super(ticks, object, doOnTick, doOnComplete, doOnCancel);
    }

    public static void add(UuidScheduledTask task)
    {
        tasks.add(task);
    }

    public static void tick()
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

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (!(obj instanceof UuidScheduledTask other)) return false;

        return Objects.equals(getObject(), other.getObject());
    }

    @Override
    public int hashCode()
    {
        return getObject() == null ? 0 : getObject().hashCode();
    }
}
