package com.pekar.callofcompanions.scheduler;

import com.pekar.callofcompanions.data.CompanionEntry;
import com.pekar.callofcompanions.scheduler.base.ScheduledTask;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class CompanionEntryTask extends ScheduledTask<CompanionEntry>
{
    public CompanionEntryTask(int ticks, CompanionEntry object, BiFunction<Integer, CompanionEntry, Boolean> doOnTick, Consumer<CompanionEntry> doOnComplete, Consumer<CompanionEntry> doOnCancel)
    {
        super(ticks, object, doOnTick, doOnComplete, doOnCancel);
    }

    public CompanionEntry getCompanionEntry()
    {
        return getObject();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (!(obj instanceof CompanionEntryTask other)) return false;

        return Objects.equals(getObject(), other.getObject());
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(getObject());
    }
}
