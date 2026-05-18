package com.pekar.callofcompanions.items;

import com.mojang.logging.LogUtils;
import com.pekar.callofcompanions.Config;
import com.pekar.callofcompanions.controllers.*;
import com.pekar.callofcompanions.data.CompanionData;
import com.pekar.callofcompanions.data.CompanionEntry;
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

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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
    public int getMaxStackSize(ItemStack stack)
    {
        return stack.get(DataRegistry.CRYSTAL_ID) != null ? 1 : super.getMaxStackSize(stack);
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

        if (!hasEnoughXp(player))
        {
            if (player instanceof ServerPlayer serverPlayer)
                serverPlayer.sendOverlayMessage(Component.translatable("message.callofcompanions.not_enough_xp"));

            return InteractionResult.FAIL;
        }

        player.getCooldowns().addCooldown(stack, crystalCooldown());

        var companionData = savedCompanionData.copy();

        if (player instanceof ServerPlayer serverPlayer)
        {
            stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);

            var serverLevel = serverPlayer.level();
            playSummonSound(serverLevel, player.blockPosition());
            showSummonParticles(serverLevel, useOnPos);

            var farTeleportListener = new TeleportListener()
            {
                private boolean farTeleportUsed = false;
                private boolean crossDimTeleportUsed = false;

                @Override
                public void onTeleport(TeleportType teleportType)
                {
                    if (teleportType == TeleportType.FAR_TELEPORT)
                        farTeleportUsed = true;
                    else if (teleportType == TeleportType.CROSS_DIMENSION_TELEPORT)
                        crossDimTeleportUsed = true;
                }

                @Override
                public boolean teleported()
                {
                    return farTeleportUsed || crossDimTeleportUsed;
                }

                @Override
                public boolean isCrossDimensional()
                {
                    return crossDimTeleportUsed;
                }
            };

            scheduleSaveDataOnTasksEnd(serverPlayer, crystalId, companionData, farTeleportListener);

            var companionList = companionData.companions();
            var iterator = companionList.iterator();
            boolean anySummoned = false;

            while (iterator.hasNext())
            {
                var companionEntry = iterator.next();
                var animalLevel = serverLevel.getServer().getLevel(companionEntry.dimension());
                if (animalLevel == null) continue;

                var entity = animalLevel.getEntity(companionEntry.uuid());
                Animal animal = entity instanceof Animal a ? a : null;

                if (!CallCrystalHelper.canSummonAnimal(entity, companionEntry.ownerUuid().orElse(null), player))
                {
                    LOGGER.debug("Skipped: companion can't be summoned by player, companionType={}, companionId={}, player={}", companionEntry.type(), companionEntry.uuid(), player.getDisplayName());

                    var animalDisplayName = CallCrystalHelper.buildAnimalName(companionEntry.type(), companionEntry.name());
                    serverPlayer.sendSystemMessage(Component.translatable("message.callofcompanions.not_owner", animalDisplayName));

                    continue;
                }

                anySummoned = true;

                var summonContext = new SummonAnimalContext(
                        serverPlayer,
                        animal,
                        companionData,
                        companionEntry,
                        stack,
                        callDelayFactor(),
                        farTeleportListener,
                        allowInterDimensionalTeleports()
                );

                AnimalSummonFactory.get(summonContext).run(useOnPos.above());
            }

            if (!anySummoned)
            {
                stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, false);
                serverPlayer.sendOverlayMessage(Component.translatable("message.callofcompanions.no_summonable_companions"));
            }
        }

        return sidedSuccess(player.level().isClientSide());
    }

    private boolean hasEnoughXp(Player player)
    {
        if (player.isCreative()) return true;
        return player.experienceLevel >= requiredXpAmountToCall();
    }

    private void consumeXp(Player player, boolean isInterDimensionalCall)
    {
        if (player.isCreative()) return;

        int levelsToConsume = isInterDimensionalCall
                ? Math.min(requiredXpAmountToCall(), Config.XP_LEVELS_TO_CONSUME_CROSS_DIMENSION.getAsInt())
                : Math.min(requiredXpAmountToCall(), Config.XP_LEVELS_TO_CONSUME.getAsInt());

        player.giveExperienceLevels(-Math.min(levelsToConsume, player.experienceLevel));
    }

    private void scheduleSaveDataOnTasksEnd(ServerPlayer serverPlayer, UUID crystalId, CompanionData companionData, TeleportListener teleportListener)
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

                    if (teleportListener.teleported())
                    {
                        consumeXp(serverPlayer, teleportListener.isCrossDimensional());
                    }
                    break;
                }
            }
        };

        CompanionEntryScheduler.listen(serverPlayer, taskEndListener);
    }

    private void updateAnimalPos(ServerPlayer serverPlayer, Animal animal)
    {
        if (!(animal.level() instanceof ServerLevel animalLevel)) return;
        for (var itemStack : serverPlayer.getInventory().getNonEquipmentItems())
        {
            if (!itemStack.is(ItemRegistry.CALL_CRYSTALS_TAG)) continue;
            var data = itemStack.get(DataRegistry.COMPANIONS);
            if (data == null) continue;
            var entry = data.getCompanion(animal.getUUID());
            if (entry == null) continue;

            var oldPos = entry.pos();
            if (entry.dimension().equals(animalLevel.dimension()) && animal.distanceToSqr(oldPos.getX(), oldPos.getY(), oldPos.getZ()) < 100)
                continue;

            CallCrystalHelper.updateCompanionPos(animalLevel, data, entry);
            itemStack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, false);
            saveStackChanges(serverPlayer, itemStack, itemStack.get(DataRegistry.CRYSTAL_ID), data);
        }
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
        var level = context.level();
        if (level == null) return;

        var companionData = stack.get(DataRegistry.COMPANIONS);

        tooltip.ignoreEmptyLines();

        if (companionData != null)
        {
            for (var companionEntry : companionData.companions())
            {
                var name = CallCrystalHelper.buildAnimalName(companionEntry.type(), companionEntry.name());
                var status = companionEntry.positionStatus() == PositionStatus.LOST ? "" : "✓";
                if (flag.hasShiftDown())
                {
                    status += getTimeString(level, companionEntry.timestamp(), companionEntry.gameTimestamp());
                }
                else if (flag.hasAltDown() && Config.TOOLTIP_SHOW_LAST_POSITION.isTrue())
                {
                    status += getAnimalLocationString(companionEntry);
                }

                String ownerName;
                if (companionEntry.ownerName().isPresent())
                    ownerName = companionEntry.ownerName().get();
                 else if (companionEntry.ownerUuid().isEmpty())
                     ownerName = Component.translatable("text.callofcompanions.none").getString();
                 else
                     ownerName = "?";

                // Determine coloring (recent / medium age) from configured time source
                AgeCategory ageCategory = computeAgeCategory(companionEntry.timestamp(), companionEntry.gameTimestamp(), level);

                tooltip.addLine(getDescriptionId(), 1)
                        .fillWith(name, ownerName, status)
                        .withFormatting(ChatFormatting.GREEN, Config.TOOLTIP_AGE_COLORING.isTrue() && ageCategory == AgeCategory.RECENT)
                        .withFormatting(ChatFormatting.WHITE, Config.TOOLTIP_AGE_COLORING.isTrue() && ageCategory == AgeCategory.MEDIUM)
                        .styledAs(TextStyle.DarkGray, companionEntry.positionStatus() == PositionStatus.LOST)
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
            tooltip.addLine(getSummonableAnimalsInfoDescriptionId(), 4)
                    .withFormatting(ChatFormatting.DARK_GREEN, true)
                    .apply();
            tooltip.addLine(getCrossDimensionCallsInfoDescriptionId(), 5)
                    .withFormatting(ChatFormatting.DARK_GREEN, true)
                    .apply();

            if (Config.CONSUME_XP_ON_CALL.isTrue())
            {
                tooltip.addLine(getDescriptionId(), 6)
                        .fillWith(requiredXpAmountToCall())
                        .withFormatting(ChatFormatting.DARK_GREEN, true)
                        .apply();
            }
        }
        else if (!flag.hasAltDown())
        {
            if (Config.TOOLTIP_SHOW_LAST_POSITION.isTrue())
                tooltip.addLineById("description.press_shift_or_alt").apply();
            else
                tooltip.addLineById("description.press_shift").apply();
        }
    }

    private String getAnimalLocationString(CompanionEntry companionEntry)
    {
        // Format position as: Pos: (-1500, 64, 100) - Overworld
        var pos = companionEntry.pos();
        String coords = "(" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ")";

        // Map common dimensions to user-friendly names
        var key = companionEntry.dimension();
        String dimName = Component.translatable("dimension." + key.identifier().getNamespace() + "." + key.identifier().getPath()).getString();
        String savedLabel = Component.translatable("text.saved_pos").getString();
        return "  " + savedLabel + " " + coords + " - " + dimName;
    }

    private String getTimeString(Level level, long timestamp, long gameTimestamp)
    {
        // If there is no real timestamp, we cannot produce meaningful output (gameTimestamp-only case is invalid here)
        if (timestamp == 0L) return "";

        boolean useReal = Config.TOOLTIP_USE_REALTIME.isTrue();

        String relative;
        String absolute = formatAbsolute(timestamp);

        if (useReal)
        {
            long secondsAgo = Math.max(0L, (System.currentTimeMillis() - timestamp) / 1000);
            relative = formatRelativeReal(secondsAgo);
        }
        else
        {
            if (gameTimestamp != 0L)
            {
                long secondsAgo = Math.max(0L, (level.getGameTime() - gameTimestamp) / 20);
                relative = formatRelativeInGame(secondsAgo);
            }
            else
            {
                // configured source missing -> hide relative and show only absolute
                relative = "";
            }
        }

        return "  " + (relative.isEmpty() ? absolute : relative + " (" + absolute + ")");
    }

    private String formatRelativeReal(long secondsAgo)
    {
        if (secondsAgo < 60)
            return secondsAgo + Component.translatable("text.callofcompanions.seconds_ago").getString();

        long minutes = secondsAgo / 60;
        if (minutes < 60)
            return minutes + Component.translatable("text.callofcompanions.minutes_ago").getString();

        long hours = minutes / 60;
        if (hours < 24)
            return hours + Component.translatable("text.callofcompanions.hours_ago").getString();

        long days = hours / 24;
        return days + Component.translatable("text.callofcompanions.days_ago").getString();
    }

    private String formatRelativeInGame(long secondsAgo)
    {
        if (secondsAgo < 60)
            return secondsAgo + Component.translatable("text.callofcompanions.seconds_ago").getString();

        long minutes = secondsAgo / 60;
        if (minutes < 20) // 20 minutes == 1 in-game day
            return minutes + Component.translatable("text.callofcompanions.minutes_ago").getString();

        long days = minutes / 20;
        return days + Component.translatable("text.callofcompanions.days_ago").getString();
    }

    private String formatAbsolute(long timestamp)
    {
        String pattern = Config.DATETIME_FORMAT.get();
        DateTimeFormatter formatter;
        try
        {
            formatter = DateTimeFormatter.ofPattern(pattern).withZone(ZoneId.systemDefault());
        }
        catch (IllegalArgumentException | DateTimeParseException ex)
        {
            formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm").withZone(ZoneId.systemDefault());
        }

        Instant instantToFormat = Instant.ofEpochMilli(timestamp);
        return formatter.format(instantToFormat);
    }

    protected String getSummonableAnimalsInfoDescriptionId()
    {
        return getDescriptionId();
    }

    protected String getCrossDimensionCallsInfoDescriptionId()
    {
        return getDescriptionId();
    }

    protected int crystalDataCapacity()
    {
        return Config.CRYSTAL_DATA_CAPACITY.getAsInt();
    }

    protected boolean allowInterDimensionalTeleports()
    {
        return false;
    }

    protected int requiredXpAmountToCall()
    {
        return Config.CONSUME_XP_ON_CALL.isTrue() ? Config.XP_LEVELS_TO_CONSUME.getAsInt() : 0;
    }

    private enum AgeCategory { NONE, RECENT, MEDIUM }

    private AgeCategory computeAgeCategory(long realTimestamp, long gameTimestamp, Level level)
    {
        boolean useReal = Config.TOOLTIP_USE_REALTIME.isTrue();

        if (useReal)
        {
            if (realTimestamp == 0L) return AgeCategory.NONE;
            long seconds = Math.max(0L, (System.currentTimeMillis() - realTimestamp) / 1000);
            if (seconds <= 120) return AgeCategory.RECENT; // <= 2 minutes
            if (seconds <= 1200) return AgeCategory.MEDIUM; // >2 and <=20 minutes
            return AgeCategory.NONE;
        }

        if (gameTimestamp == 0L) return AgeCategory.NONE;
        long age = level.getGameTime() - gameTimestamp; // ticks
        if (age <= 2400L) return AgeCategory.RECENT; // <= 2 minutes (in ticks)
        if (age <= 24_000L) return AgeCategory.MEDIUM; // >2 and <=20 minutes
        return AgeCategory.NONE;
    }
}