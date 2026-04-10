package com.pekar.callofcompanions.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static com.pekar.callofcompanions.data.CompanionEntry.ENTRY_CODEC;

public class CompanionData
{
    private static final int MAX_COMPANIONS = 8;
    private final UUID uuid;
    private final List<CompanionEntry> companions;

    public static final Codec<CompanionData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    UUIDUtil.CODEC.fieldOf("uuid").forGetter(CompanionData::uuid),
                    ENTRY_CODEC.listOf().fieldOf("companions").forGetter(CompanionData::companions)
            ).apply(instance, CompanionData::new)
    );

    public CompanionData()
    {
        this(UUID.randomUUID(), new ArrayList<>());
    }

    private CompanionData(UUID uuid, List<CompanionEntry> companions)
    {
        this.uuid = uuid;
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

    public UUID uuid()
    {
        return this.uuid;
    }

    public List<CompanionEntry> companions()
    {
        return List.copyOf(companions);
    }

    public CompanionData copy()
    {
        return new CompanionData(uuid, List.copyOf(companions));
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (!(obj instanceof CompanionData other)) return false;
        return Objects.equals(this.uuid, other.uuid) && this.companions.equals(other.companions);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(uuid, companions);
    }
}