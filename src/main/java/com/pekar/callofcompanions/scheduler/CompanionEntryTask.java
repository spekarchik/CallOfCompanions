package com.pekar.callofcompanions.scheduler;

import com.pekar.callofcompanions.data.CompanionEntry;
import com.pekar.callofcompanions.scheduler.base.ScheduledTask;
import net.minecraft.server.level.ServerPlayer;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class CompanionEntryTask extends ScheduledTask<CompanionEntry>
{
    private final ServerPlayer initiator;

    public CompanionEntryTask(int ticks, CompanionEntry object, ServerPlayer initiator, BiFunction<Integer, CompanionEntry, Boolean> doOnTick, Consumer<CompanionEntry> doOnComplete, Consumer<CompanionEntry> doOnCancel)
    {
        super(ticks, object, doOnTick, doOnComplete, doOnCancel);
        this.initiator = initiator;
    }

    public CompanionEntry companionEntry()
    {
        return getObject();
    }

    public ServerPlayer initiator()
    {
        return initiator;
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
