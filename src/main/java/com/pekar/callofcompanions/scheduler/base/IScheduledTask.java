package com.pekar.callofcompanions.scheduler.base;

public interface IScheduledTask
{
    void decrementOrExecute();
    void execute();
    void cancel();
    boolean isCompleted();
    int getCounter();
}
