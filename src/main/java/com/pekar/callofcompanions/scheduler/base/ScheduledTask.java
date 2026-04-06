package com.pekar.callofcompanions.scheduler.base;

import java.util.function.Consumer;
import java.util.function.Function;

public class ScheduledTask<T> implements IScheduledTask
{
    private final T object;
    private final Consumer<T> doOnComplete;
    private final Consumer<T> doOnCancel;
    private final Function<T, Boolean> doOnTick;
    private int counter;
    private boolean isCompleted;

    public ScheduledTask(int ticks, T object, Consumer<T> doOnComplete)
    {
        this(ticks, object, (Function<T, Boolean>) null, doOnComplete);
    }

    public ScheduledTask(int ticks, T object, Consumer<T> doOnComplete, Consumer<T> doOnCancel)
    {
        this(ticks, object, null, doOnComplete, doOnCancel);
    }

    public ScheduledTask(int ticks, T object, Function<T, Boolean> doOnTick, Consumer<T> doOnComplete)
    {
        this(ticks, object, doOnTick, doOnComplete, null);
    }

    public ScheduledTask(int ticks, T object, Function<T, Boolean> doOnTick, Consumer<T> doOnComplete, Consumer<T> doOnCancel)
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
            isCompleted = true;
        }
    }

    @Override
    public void cancel()
    {
        if (!isCompleted())
        {
            if (doOnCancel != null) doOnCancel.accept(object);
            isCompleted = true;
        }
    }

    private void executeOnTick()
    {
        if (!isCompleted())
        {
            var result = doOnTick.apply(object);
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
