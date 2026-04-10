package com.pekar.callofcompanions.scheduler.base;

import com.mojang.logging.LogUtils;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import org.slf4j.Logger;

public class ScheduledTask<T> implements IScheduledTask
{
    private static final Logger LOGGER = LogUtils.getLogger();
    private final T object;
    private final Consumer<T> doOnComplete;
    private final Consumer<T> doOnCancel;
    private final BiFunction<Integer, T, Boolean> doOnTick;
    private int counter;
    private boolean isCompleted;

    public ScheduledTask(int ticks, T object, Consumer<T> doOnComplete)
    {
        this(ticks, object, (BiFunction<Integer, T, Boolean>) null, doOnComplete);
    }

    public ScheduledTask(int ticks, T object, Consumer<T> doOnComplete, Consumer<T> doOnCancel)
    {
        this(ticks, object, null, doOnComplete, doOnCancel);
    }

    public ScheduledTask(int ticks, T object, BiFunction<Integer, T, Boolean> doOnTick, Consumer<T> doOnComplete)
    {
        this(ticks, object, doOnTick, doOnComplete, null);
    }

    public ScheduledTask(int ticks, T object, BiFunction<Integer, T, Boolean> doOnTick, Consumer<T> doOnComplete, Consumer<T> doOnCancel)
    {
        this.counter = ticks;
        this.object = object;
        this.doOnTick = doOnTick;
        this.doOnComplete = doOnComplete;
        this.doOnCancel = doOnCancel;
    }

    @Override
    public final void decrementOrExecute()
    {
        if (--counter <= 0) execute();
        else if (doOnTick != null) executeOnTick();
    }

    @Override
    public final void execute()
    {
        if (!isCompleted())
        {
            doOnComplete.accept(object);
            LOGGER.debug("Scheduled task completed: taskType={}, objectType={}, remainingTicks={}",
                    getClass().getSimpleName(),
                    object != null ? object.getClass().getSimpleName() : "null",
                    counter);
            isCompleted = true;
        }
    }

    @Override
    public void cancel()
    {
        if (!isCompleted())
        {
            if (doOnCancel != null) doOnCancel.accept(object);
            LOGGER.debug("Scheduled task cancelled: taskType={}, objectType={}, remainingTicks={}",
                    getClass().getSimpleName(),
                    object != null ? object.getClass().getSimpleName() : "null",
                    counter);
            isCompleted = true;
        }
    }

    private void executeOnTick()
    {
        if (!isCompleted())
        {
            var result = doOnTick.apply(counter, object);
            if (result) execute();
        }
    }

    @Override
    public final boolean isCompleted()
    {
        return isCompleted;
    }

    @Override
    public int getCounter()
    {
        return counter;
    }

    protected T getObject()
    {
        return object;
    }
}
