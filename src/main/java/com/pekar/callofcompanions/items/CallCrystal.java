package com.pekar.callofcompanions.items;

import com.pekar.callofcompanions.data.CompanionData;
import com.pekar.callofcompanions.data.CompanionEntry;
import com.pekar.callofcompanions.data.DataRegistry;
import com.pekar.callofcompanions.data.PositionStatus;
import com.pekar.callofcompanions.network.SaveCompanionsPacket;
import com.pekar.callofcompanions.scheduler.UuidScheduledTask;
import com.pekar.callofcompanions.tooltip.ITooltip;
import com.pekar.callofcompanions.tooltip.ITooltipProvider;
import com.pekar.callofcompanions.tooltip.TextStyle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.Ticket;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.UUID;
import java.util.function.Consumer;

public class CallCrystal extends ModItem implements ITooltipProvider
{
    public CallCrystal(Properties properties)
    {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand)
    {
        var stack = player.getItemInHand(hand);
        var companions = stack.get(DataRegistry.COMPANIONS);
        if (companions == null || companions.getCompanions().isEmpty()) return InteractionResult.PASS;

        if (level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer)
        {
            var companionList = companions.getCompanions();
            var iterator = companionList.iterator();
            boolean companionsUpdated = false;
            while (iterator.hasNext())
            {
                var companion = iterator.next();
                updateCompanionPos(serverLevel, companions, companion);
                companionsUpdated = true;
            }

            if (companionsUpdated)
                saveStackChanges(serverPlayer, stack, companions);
        }

        return sidedSuccess(level.isClientSide());
    }

    @Override
    public InteractionResult useOn(UseOnContext context)
    {
        var player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;

        var stack = context.getItemInHand();
        var companions = stack.get(DataRegistry.COMPANIONS);
        if (companions == null || companions.getCompanions().isEmpty()) return InteractionResult.PASS;
        var level = context.getLevel();

        if (level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer && context.getClickedFace() == Direction.UP)
        {
            var companionList = companions.getCompanions();
            var iterator = companionList.iterator();

            while (iterator.hasNext())
            {
                var companion = iterator.next();
                boolean teleported = tryTeleportAnimalTo(serverLevel, companion.uuid(), context.getClickedPos());
                if (teleported)
                {
                    updateCompanionPos(serverLevel, companions, companion);
                    saveStackChanges(serverPlayer, stack, companions);
                    continue;
                }

                if (serverLevel.dimension().equals(companion.dimension()))
                {
                    var chunkPos = new ChunkPos(SectionPos.blockToSectionCoord(companion.pos().getX()), SectionPos.blockToSectionCoord(companion.pos().getZ()));
                    var ticket = new Ticket(TicketType.PORTAL, 2);
                    serverLevel.getChunkSource().addTicket(ticket, chunkPos);

                    var task = new UuidScheduledTask(
                            300,
                            companion.uuid(),
                            uuid -> checkEntityLoaded(serverLevel, uuid),
                            uuid ->
                            {
                                tryTeleportAnimalTo(serverLevel, uuid, context.getClickedPos());
                                updateCompanionPos(serverLevel, companions, companion);
                                saveStackChanges(serverPlayer, stack, companions);
                                serverLevel.getChunkSource().removeTicketWithRadius(TicketType.PORTAL, chunkPos, 20);
                            },
                            uuid ->
                                    serverLevel.getChunkSource().removeTicketWithRadius(TicketType.PORTAL, chunkPos, 20));

                    UuidScheduledTask.add(task);
                }
            }
        }

        return sidedSuccess(player.level().isClientSide());
    }

    private void updateCompanionPos(ServerLevel level, CompanionData companions, CompanionEntry companion)
    {
        var entity = level.getEntity(companion.uuid());
        if (entity == null)
        {
            companions.add(companion.getAsLost());
            return;
        }

        var newEntry = companion.getWith(entity.blockPosition());
        companions.add(newEntry);
    }

    private void saveStackChanges(ServerPlayer serverPlayer, ItemStack stack, CompanionData companions)
    {
        var companionsCopy = companions.copy();
        stack.remove(DataRegistry.COMPANIONS);
        stack.set(DataRegistry.COMPANIONS, companionsCopy);
        new SaveCompanionsPacket(companionsCopy).sendToPlayer(serverPlayer);
    }

    private boolean checkEntityLoaded(Level level, UUID uuid)
    {
        var entity = level.getEntity(uuid);
        return entity != null;
    }

    private boolean tryTeleportAnimalTo(Level level, UUID uuid, BlockPos pos)
    {
        var entity = level.getEntity(uuid);
        System.out.println("  Trying to teleport. Is null: " + (entity == null));
        if (entity instanceof Animal animal)
        {
            if (entity instanceof TamableAnimal tamable)
            {
                if (tamable.isInSittingPose())
                {
                    tamable.setOrderedToSit(false);
                    tamable.setInSittingPose(false);
                }
            }
            animal.teleportTo(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5);
            return true;
        }

        return false;
    }

    @Override
    public void appendHoverText(ItemStack itemStack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag tooltipFlag)
    {
        ITooltipProvider.appendHoverText(this, itemStack, context, display, builder, tooltipFlag);
    }

    @Override
    public void addTooltip(ItemStack stack, TooltipContext context, ITooltip tooltip, TooltipFlag flag)
    {
        var companions = stack.get(DataRegistry.COMPANIONS);
        if (companions == null) return;

        for (var companion : companions.getCompanions())
        {
            var name = buildName(companion.type(), companion.name());
            var status = companion.positionStatus() == PositionStatus.LOST ? "✖ " : "✓";

            tooltip.addLine(getDescriptionId(), 1)
                    .fillWith(name, companion.ownerName(), status)
                    .styledAs(TextStyle.DarkGray, companion.positionStatus() == PositionStatus.LOST)
                    .apply();
        }
    }

    private String buildName(String animalType, String animalName)
    {
        return animalName.equals(animalType) ? animalType : animalType + " '" + animalName + "'";
    }
}
