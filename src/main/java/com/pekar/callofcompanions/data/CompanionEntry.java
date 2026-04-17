package com.pekar.callofcompanions.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public record CompanionEntry(UUID uuid, String name, String type, ResourceKey<Level> dimension, BlockPos pos, PositionStatus positionStatus, Optional<UUID> ownerUuid, Optional<String> ownerName)
{
    public static final Codec<CompanionEntry> ENTRY_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    UUIDUtil.CODEC.fieldOf("uuid").forGetter(CompanionEntry::uuid),
                    Codec.STRING.fieldOf("name").forGetter(CompanionEntry::name),
                    Codec.STRING.fieldOf("type").forGetter(CompanionEntry::type),
                    ResourceKey.codec(Registries.DIMENSION).fieldOf("dimension").forGetter(CompanionEntry::dimension),
                    BlockPos.CODEC.fieldOf("pos").forGetter(CompanionEntry::pos),
                    Codec.STRING.xmap(PositionStatus::valueOf, PositionStatus::name).fieldOf("positionStatus").forGetter(CompanionEntry::positionStatus),
                    UUIDUtil.CODEC.optionalFieldOf("ownerUuid").forGetter(CompanionEntry::ownerUuid),
                    Codec.STRING.optionalFieldOf("ownerName").forGetter(CompanionEntry::ownerName)
                    ).apply(instance, CompanionEntry::new)
    );

    // need to also update animal type because text localization may be changed
    public CompanionEntry getWith(ResourceKey<Level> dimension, BlockPos newPos, String animalType)
    {
        return new CompanionEntry(this.uuid, this.name, animalType, dimension, newPos, PositionStatus.FRESH, this.ownerUuid, this.ownerName);
    }

    public CompanionEntry getAsLost()
    {
        if (positionStatus == PositionStatus.LOST) return this;
        return new CompanionEntry(this.uuid, this.name, this.type, this.dimension, this.pos, PositionStatus.LOST, this.ownerUuid, this.ownerName);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof CompanionEntry other)) return false;
        return Objects.equals(this.uuid, other.uuid);
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(this.uuid);
    }
}
