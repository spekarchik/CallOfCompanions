package com.pekar.callofcompanions.items;

import com.pekar.callofcompanions.controllers.SummonAnimalContext;
import com.pekar.callofcompanions.controllers.SummonAnimalController;
import com.pekar.callofcompanions.controllers.SummonAnimalControllerFactory;
import com.pekar.callofcompanions.data.DataRegistry;
import com.pekar.callofcompanions.data.PositionStatus;
import com.pekar.callofcompanions.tooltip.ITooltip;
import com.pekar.callofcompanions.tooltip.ITooltipProvider;
import com.pekar.callofcompanions.tooltip.TextStyle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.FAIL;

        var stack = player.getItemInHand(hand);
        var companions = stack.get(DataRegistry.COMPANIONS);
        if (companions == null || companions.getCompanions().isEmpty()) return InteractionResult.FAIL;

        if (level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer)
        {
            var companionList = companions.getCompanions();
            var iterator = companionList.iterator();
            boolean companionsUpdated = false;
            while (iterator.hasNext())
            {
                var companion = iterator.next();
                SummonAnimalController.updateCompanionPos(serverLevel, companions, companion);
                companionsUpdated = true;
            }

            if (companionsUpdated)
                SummonAnimalController.saveStackChanges(serverPlayer, stack, companions);
        }

        return sidedSuccess(level.isClientSide());
    }

    @Override
    public InteractionResult useOn(UseOnContext context)
    {
        final int USE_CRYSTAL_COOLDOWN = 400;

        var player = context.getPlayer();
        if (player == null || context.getHand() != InteractionHand.MAIN_HAND) return InteractionResult.FAIL;

        var stack = context.getItemInHand();
        var companionData = stack.get(DataRegistry.COMPANIONS);
        if (companionData == null || companionData.getCompanions().isEmpty()) return InteractionResult.FAIL;
        var level = context.getLevel();

        if (context.getClickedFace() != Direction.UP || player.getCooldowns().isOnCooldown(stack)) return InteractionResult.FAIL;

        player.getCooldowns().addCooldown(stack, USE_CRYSTAL_COOLDOWN);

        if (player instanceof ServerPlayer serverPlayer)
        {
            var serverLevel = serverPlayer.level();
            var companionList = companionData.getCompanions();
            var iterator = companionList.iterator();
            var clickPos = context.getClickedPos();
            playSummonSound(serverLevel, player.blockPosition());
            showSummonParticles(serverLevel, clickPos);

            while (iterator.hasNext())
            {
                var companionEntry = iterator.next();
                var entity = level.getEntity(companionEntry.uuid());
                Animal animal = entity instanceof Animal a ? a : null;

                var summonContext = new SummonAnimalContext(
                        serverPlayer,
                        animal,
                        companionData,
                        companionEntry,
                        stack
                );

                SummonAnimalControllerFactory.get(summonContext).run(clickPos);
            }
        }

        return sidedSuccess(player.level().isClientSide());
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
            var name = SummonAnimalController.buildAnimalName(companion.type(), companion.name());
            var status = companion.positionStatus() == PositionStatus.LOST ? "" : "✓";

            tooltip.addLine(getDescriptionId(), 1)
                    .fillWith(name, companion.ownerName(), status)
                    .styledAs(TextStyle.DarkGray, companion.positionStatus() == PositionStatus.LOST)
                    .apply();
        }
    }
}
