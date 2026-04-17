package com.pekar.callofcompanions.items;

import com.mojang.logging.LogUtils;
import com.pekar.callofcompanions.Config;
import com.pekar.callofcompanions.controllers.AnimalSummonFactory;
import com.pekar.callofcompanions.controllers.CallCrystalHelper;
import com.pekar.callofcompanions.controllers.SummonAnimalContext;
import com.pekar.callofcompanions.data.CompanionData;
import com.pekar.callofcompanions.data.DataRegistry;
import com.pekar.callofcompanions.data.PositionStatus;
import com.pekar.callofcompanions.network.SaveCompanionsPacket;
import com.pekar.callofcompanions.scheduler.CompanionEntryScheduler;
import com.pekar.callofcompanions.scheduler.TaskEndListener;
import com.pekar.callofcompanions.tooltip.ITooltip;
import com.pekar.callofcompanions.tooltip.ITooltipProvider;
import com.pekar.callofcompanions.tooltip.TextStyle;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;

import java.util.UUID;
import java.util.function.Consumer;

public class CallCrystal extends ModItem implements ITooltipProvider
{
    private static final Logger LOGGER = LogUtils.getLogger();

    public CallCrystal(Properties properties)
    {
        super(properties);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged)
    {
        return slotChanged;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand)
    {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.FAIL;

        var stack = player.getItemInHand(hand);
        var companionData = stack.get(DataRegistry.COMPANIONS);
        if (companionData == null || companionData.companions().isEmpty()) return InteractionResult.FAIL;
        var crystalId = stack.get(DataRegistry.CRYSTAL_ID);
        if (crystalId == null) return InteractionResult.FAIL;

        if (level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer)
        {
            playUpdateCrystalSound(serverLevel, player.blockPosition());

            var companionList = companionData.companions();
            var iterator = companionList.iterator();
            boolean companionsUpdated = false;
            while (iterator.hasNext())
            {
                var companion = iterator.next();
                CallCrystalHelper.updateCompanionPos(serverLevel, companionData, companion);
                companionsUpdated = true;
            }

            if (companionsUpdated)
            {
                saveStackChanges(serverPlayer, stack, crystalId, companionData);
                serverPlayer.sendOverlayMessage(Component.translatable("message.callofcompanions.companions_updated"));
            }
        }

        return sidedSuccess(level.isClientSide());
    }

    @Override
    public InteractionResult useOn(UseOnContext context)
    {
        var player = context.getPlayer();
        if (player == null || context.getHand() != InteractionHand.MAIN_HAND) return InteractionResult.FAIL;

        var stack = context.getItemInHand();
        var savedCompanionData = stack.get(DataRegistry.COMPANIONS);
        if (savedCompanionData == null || savedCompanionData.companions().isEmpty()) return InteractionResult.FAIL;
        var crystalId = stack.get(DataRegistry.CRYSTAL_ID);
        if (crystalId == null) return InteractionResult.FAIL;

        if (player.getCooldowns().isOnCooldown(stack)) return InteractionResult.FAIL;

        var level = context.getLevel();
        var clickPos = context.getClickedPos();
        var clickedTopFace = context.getClickedFace() == Direction.UP;
        var hasNoCollisions = CallCrystalHelper.noCollisionOrIsWater(level, clickPos);

        if (!clickedTopFace && !hasNoCollisions) return InteractionResult.FAIL;

        var useOnPos = hasNoCollisions ? clickPos.below() : clickPos;
        if (!CallCrystalHelper.canApplyCrystalAt(level, useOnPos.above())) return InteractionResult.FAIL;

        if (!consumeXp(player)) return InteractionResult.FAIL;

        player.getCooldowns().addCooldown(stack, crystalCooldown());

        var companionData = savedCompanionData.copy();

        if (player instanceof ServerPlayer serverPlayer)
        {
            stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);

            var serverLevel = serverPlayer.level();
            playSummonSound(serverLevel, player.blockPosition());
            showSummonParticles(serverLevel, useOnPos);

            ScheduleSaveDataOnTasksEnd(serverPlayer, crystalId, companionData);

            var companionList = companionData.companions();
            var iterator = companionList.iterator();

            while (iterator.hasNext())
            {
                var companionEntry = iterator.next();
                var entity = level.getEntity(companionEntry.uuid());
                Animal animal = entity instanceof Animal a ? a : null;

                if (!CallCrystalHelper.canSummonAnimal(entity, player))
                {
                    LOGGER.debug("Skipped: companion can't be summoned by player, companionType={}, companionId={}, player={}", companionEntry.type(), companionEntry.uuid(), player.getDisplayName());
                    continue;
                }

                var summonContext = new SummonAnimalContext(
                        serverPlayer,
                        animal,
                        companionData,
                        companionEntry,
                        stack,
                        callDelayFactor()
                );

                AnimalSummonFactory.get(summonContext).run(useOnPos.above());
            }
        }

        return sidedSuccess(player.level().isClientSide());
    }

    private static boolean consumeXp(Player player)
    {
        if (Config.CONSUME_XP_ON_CALL.isTrue() && !player.isCreative())
        {
            int levelsToConsume = Config.XP_LEVELS_TO_CONSUME.getAsInt();

            if (player.experienceLevel < levelsToConsume)
            {
                if (player instanceof ServerPlayer serverPlayer)
                    serverPlayer.sendOverlayMessage(Component.translatable("message.callofcompanions.not_enough_xp"));

                return false;
            }

            player.giveExperienceLevels(-levelsToConsume);
        }

        return true;
    }

    private void ScheduleSaveDataOnTasksEnd(ServerPlayer serverPlayer, UUID crystalId, CompanionData companionData)
    {
        var taskEndListener = new TaskEndListener()
        {
            @Override
            public void onAllTasksEnd()
            {
                for (var itemStack : serverPlayer.getInventory().getNonEquipmentItems())
                {
                    if (!CallCrystalHelper.hasSameId(itemStack, crystalId)) continue;

                    itemStack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, false);
                    saveStackChanges(serverPlayer, itemStack, crystalId, companionData);
                    break;
                }
            }
        };

        CompanionEntryScheduler.listen(serverPlayer, taskEndListener);
    }

    private void saveStackChanges(ServerPlayer serverPlayer, ItemStack stack, UUID crystalId, CompanionData companionData)
    {
        LOGGER.debug("Saving call crystal companion data: player={}, crystalId={}, companionCount={}",
                serverPlayer.getName().getString(),
                crystalId,
                companionData.companions().size());
        var data = companionData.copy();
        stack.remove(DataRegistry.COMPANIONS);
        stack.set(DataRegistry.COMPANIONS, data);
        new SaveCompanionsPacket(crystalId, data).sendToPlayer(serverPlayer);
    }

    private void showSummonParticles(ServerLevel serverLevel, BlockPos clickPos)
    {
        serverLevel.sendParticles(
                ParticleTypes.ELECTRIC_SPARK,
                clickPos.getX(), clickPos.getY(), clickPos.getZ(),
                100,
                0.5, 0.5, 0.5,
                0.1
        );
    }

    private void playSummonSound(ServerLevel level, BlockPos pos)
    {
        level.playSound(null, pos, SoundEvents.AMETHYST_CLUSTER_HIT, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    private void playUpdateCrystalSound(ServerLevel level, BlockPos pos)
    {
        level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 4.0F, 0.6F);
    }

    protected int crystalCooldown()
    {
        return Config.CRYSTAL_COOLDOWN.getAsInt();
    }

    protected float callDelayFactor()
    {
        return 2F;
    }

    @Override
    public void appendHoverText(ItemStack itemStack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag tooltipFlag)
    {
        ITooltipProvider.appendHoverText(this, itemStack, context, display, builder, tooltipFlag);
    }

    @Override
    public void addTooltip(ItemStack stack, TooltipContext context, ITooltip tooltip, TooltipFlag flag)
    {
        var companionData = stack.get(DataRegistry.COMPANIONS);

        tooltip.ignoreEmptyLines();

        if (companionData != null)
        {
            for (var companion : companionData.companions())
            {
                var name = CallCrystalHelper.buildAnimalName(companion.type(), companion.name());
                var status = companion.positionStatus() == PositionStatus.LOST ? "" : "✓";
                var ownerName = companion.ownerName().isPresent()
                        ? companion.ownerName().get()
                        : Component.translatable("item.callofcompanions.deep_call_crystal.desc0").getString();

                tooltip.addLine(getDescriptionId(), 1)
                        .fillWith(name, ownerName, status)
                        .styledAs(TextStyle.DarkGray, companion.positionStatus() == PositionStatus.LOST)
                        .apply();
            }
        }

        tooltip.addEmptyLine();

        if (flag.hasShiftDown())
        {
            int companionsAdded = companionData != null ? companionData.companions().size() : 0;
            int dataCapacity = companionData != null ? companionData.capacity() : crystalDataCapacity();
            long lostCampanions = companionData != null
                    ? companionData.companions().stream().filter(c -> c.positionStatus() == PositionStatus.LOST).count()
                    : 0;

            tooltip.addLine(getDescriptionId(), 2)
                    .fillWith(companionsAdded, dataCapacity)
                    .withFormatting(ChatFormatting.DARK_AQUA, true)
                    .apply();
            tooltip.addLine(getDescriptionId(), 3)
                    .fillWith(lostCampanions)
                    .withFormatting(ChatFormatting.DARK_AQUA, true)
                    .apply();
            tooltip.addLine(getSummonableAnimalsDescriptionId(), 4)
                    .withFormatting(ChatFormatting.DARK_AQUA, true)
                    .apply();
        }
        else
        {
            tooltip.addLineById("description.press_shift").apply();
        }
    }

    protected String getSummonableAnimalsDescriptionId()
    {
        return getDescriptionId();
    }

    protected int crystalDataCapacity()
    {
        return Config.CRYSTAL_DATA_CAPACITY.getAsInt();
    }
}