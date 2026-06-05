package com.pekar.callofcompanions.events;

import com.pekar.callofcompanions.Config;
import com.pekar.callofcompanions.controllers.CallCrystalHelper;
import com.pekar.callofcompanions.data.CompanionData;
import com.pekar.callofcompanions.data.CompanionEntry;
import com.pekar.callofcompanions.data.DataRegistry;
import com.pekar.callofcompanions.data.PositionStatus;
import com.pekar.callofcompanions.items.ItemRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;
import java.util.UUID;

public final class PlayerInteractionHandler
{
    private PlayerInteractionHandler()
    {}

    public static void onPlayerInteractionEvent(EntityInteractSpecific event)
    {
        var target = event.getTarget();
        if (target instanceof Player) return;

        var itemStack = event.getItemStack();
        var player = event.getEntity();

        boolean isDeepCallCrystal = itemStack.is(ItemRegistry.DEEP_CALL_CRYSTAL);
        if (isDeepCallCrystal || itemStack.is(ItemRegistry.CALL_CRYSTAL))
        {
            if (player.getCooldowns().isOnCooldown(itemStack))
            {
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.CONSUME);
                return;
            }

            boolean isTameAnimal = target instanceof TamableAnimal tamable && tamable.isTame();
            boolean isTamedHorse = target instanceof AbstractHorse horse && horse.isTamed();

            if (target instanceof Animal animal)
            {
                if (itemStack.getCount() != 1)
                {
                    if (player instanceof ServerPlayer serverPlayer)
                    {
                        serverPlayer.sendOverlayMessage(Component.translatable("message.callofcompanions.single_crystal_only"));
                    }

                    event.setCanceled(true);
                    event.setCancellationResult(InteractionResult.CONSUME);
                    return;
                }

                if (isTameAnimal || isTamedHorse || (isDeepCallCrystal && Config.DEEP_CRYSTAL_ALLOW_UNTAMED.isTrue() && animal.hasCustomName()))
                {
                    short dataCapacity = isDeepCallCrystal ? (short) Config.DEEP_CRYSTAL_DATA_CAPACITY.getAsInt() : (short) Config.CRYSTAL_DATA_CAPACITY.getAsInt();
                    var companionData = itemStack.getOrDefault(DataRegistry.COMPANIONS, new CompanionData(dataCapacity));
                    var id = itemStack.get(DataRegistry.CRYSTAL_ID);
                    if (id == null)
                        itemStack.set(DataRegistry.CRYSTAL_ID, UUID.randomUUID());

                    var name = target.getDisplayName().getString();
                    var companionType = CallCrystalHelper.getAnimalType(animal);
                    var owner = target instanceof OwnableEntity ownable ? ownable.getOwner() : null;
                    var ownerId = target instanceof OwnableEntity ownable && ownable.getOwnerReference() != null ? ownable.getOwnerReference().getUUID() : null;
                    Optional<UUID> ownerIdOpt = ownerId != null ? Optional.of(ownerId) : Optional.empty();
                    Optional<String> ownerName = owner != null ? Optional.of(owner.getDisplayName().getString()) : Optional.empty();
                    var level = event.getLevel();

                    var entry = new CompanionEntry(
                            target.getUUID(),
                            name,
                            companionType,
                            target.level().dimension(),
                            target.blockPosition(),
                            PositionStatus.FRESH,
                            ownerIdOpt,
                            ownerName,
                            System.currentTimeMillis(),
                            level.getGameTime());

                    var result = companionData.add(entry);
                    if (result)
                    {
                        if (level instanceof ServerLevel serverLevel)
                        {
                            playAddAnimalSound(serverLevel, animal);
                        }

                        itemStack.remove(DataRegistry.COMPANIONS);
                        itemStack.set(DataRegistry.COMPANIONS, companionData.copy());

                        event.setCanceled(true);
                        event.setCancellationResult(event.getSide() == LogicalSide.CLIENT ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER);
                        return;
                    }
                    else if (player instanceof ServerPlayer serverPlayer)
                    {
                        serverPlayer.sendOverlayMessage(Component.translatable("message.callofcompanions.limit_reached"));
                    }
                }
                else
                {
                    if (player instanceof ServerPlayer serverPlayer)
                    {
                        if (isDeepCallCrystal && Config.DEEP_CRYSTAL_ALLOW_UNTAMED.isTrue())
                        {
                            serverPlayer.sendOverlayMessage(Component.translatable("message.callofcompanions.cant_bind_tame_or_named"));
                        }
                        else
                        {
                            serverPlayer.sendOverlayMessage(Component.translatable("message.callofcompanions.cant_bind_tame_only"));
                        }
                    }
                }
            }
            else if (target instanceof net.minecraft.world.entity.LivingEntity && player instanceof ServerPlayer serverPlayer)
            {
                serverPlayer.sendOverlayMessage(Component.translatable("message.callofcompanions.cant_bind_entity_type", target.getDisplayName()));
            }

            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.CONSUME);
        }
    }

    private static void playAddAnimalSound(ServerLevel level, Animal animal)
    {
        level.playSound(null, animal.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.1F, 1.6F);
    }
}

