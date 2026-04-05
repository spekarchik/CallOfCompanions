package com.pekar.callofcompanions.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;

import java.util.UUID;

public record CompanionEntry(UUID uuid, String name, String type, UUID ownerUuid, String ownerName)
{

    public static final Codec<CompanionEntry> ENTRY_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    UUIDUtil.CODEC.fieldOf("uuid").forGetter(CompanionEntry::uuid),
                    Codec.STRING.fieldOf("name").forGetter(CompanionEntry::name),
                    Codec.STRING.fieldOf("type").forGetter(CompanionEntry::type),
                    UUIDUtil.CODEC.fieldOf("ownerUuid").forGetter(CompanionEntry::ownerUuid),
                    Codec.STRING.fieldOf("ownerName").forGetter(CompanionEntry::ownerName)
                    ).apply(instance, CompanionEntry::new)
    );
}
