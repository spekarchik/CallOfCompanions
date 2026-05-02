package com.pekar.callofcompanions.controllers;

import com.pekar.callofcompanions.data.CompanionData;
import com.pekar.callofcompanions.data.CompanionEntry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;

public record SummonAnimalContext(ServerPlayer player,
                                  Animal animal,
                                  CompanionData companionData,
                                  CompanionEntry companionEntry,
                                  ItemStack callCrystalStack,
                                  float callDelayFactor,
                                  TeleportListener teleportListener
                                  )
{
    public ServerLevel level()
    {
        return (ServerLevel) player.level();
    }
}