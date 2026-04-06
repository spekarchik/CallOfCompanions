package com.pekar.callofcompanions.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.ArrayList;
import java.util.List;

import static com.pekar.callofcompanions.data.CompanionEntry.ENTRY_CODEC;

public class CompanionData
{
    private static final int MAX_COMPANIONS = 8;
    private final List<CompanionEntry> companions;

    public static final Codec<CompanionData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ENTRY_CODEC.listOf().fieldOf("companions").forGetter(CompanionData::getCompanions)
            ).apply(instance, CompanionData::new)
    );

    public CompanionData()
    {
        this(new ArrayList<>());
    }

    public CompanionData(List<CompanionEntry> companions)
    {
        this.companions = new ArrayList<>(companions);
    }

    public boolean add(CompanionEntry companionEntry)
    {
        if (companions.size() >= MAX_COMPANIONS && !companions.contains(companionEntry))
            return false;

        if (companions.contains(companionEntry))
            companions.remove(companionEntry);

        companions.add(companionEntry);
        return true;
    }

    public void remove(CompanionEntry companionEntry)
    {
        companions.remove(companionEntry);
    }

    public List<CompanionEntry> getCompanions()
    {
        return List.copyOf(companions);
    }

    public CompanionData copy()
    {
        return new CompanionData(List.copyOf(companions));
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (!(obj instanceof CompanionData other)) return false;

        return companions.equals(other.companions);
    }

    @Override
    public int hashCode()
    {
        return companions.hashCode();
    }
}