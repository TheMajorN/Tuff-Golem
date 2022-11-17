package com.themajorn.tuffgolem.common.ai;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import com.themajorn.tuffgolem.common.ai.behaviors.*;
import com.themajorn.tuffgolem.common.entities.TuffGolemEntity;
import com.themajorn.tuffgolem.core.registry.ModActivities;
import com.themajorn.tuffgolem.core.registry.ModEntities;
import com.themajorn.tuffgolem.core.registry.ModMemoryModules;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;

import java.util.Optional;

public class TuffGolemAi {

    private static final UniformInt TIME_BETWEEN_ANIMATE_OR_PETRIFY = UniformInt.of(2400, 3600);
    private static final UniformInt TIME_BETWEEN_GOING_TO_ITEM_FRAME = UniformInt.of(1200, 1800);

    public static void initMemories(TuffGolemEntity tuffGolem, RandomSource random) {
        tuffGolem.getBrain().setMemory(ModMemoryModules.ANIMATE_OR_PETRIFY_COOLDOWN_TICKS.get(), TIME_BETWEEN_ANIMATE_OR_PETRIFY.sample(random));
        tuffGolem.getBrain().setMemory(ModMemoryModules.GO_TO_ITEM_FRAME_COOLDOWN_TICKS.get(), TIME_BETWEEN_GOING_TO_ITEM_FRAME.sample(random));
    }

    public static Brain<?> makeBrain(Brain<TuffGolemEntity> brain) {
        initCoreActivity(brain);
        initIdleActivity(brain);
        initTakeItemFromFrameActivity(brain);
        initReturnItemToFrameActivity(brain);
        initPetrifyOrAnimateActivity(brain);
        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.useDefaultActivity();
        return brain;
    }

    private static void initCoreActivity(Brain<TuffGolemEntity> brain) {
        brain.addActivity(Activity.CORE, 0,
            ImmutableList.of(
                new TuffGolemLookAtTargetSink(45, 90),
                new TuffGolemMoveToTargetSink(),
                new CountDownCooldownTicks(ModMemoryModules.ANIMATE_OR_PETRIFY_COOLDOWN_TICKS.get()),
                new CountDownCooldownTicks(ModMemoryModules.GO_TO_ITEM_FRAME_COOLDOWN_TICKS.get()),
                new CountDownCooldownTicks(MemoryModuleType.TEMPTATION_COOLDOWN_TICKS)
            ));
    }

    private static void initIdleActivity(Brain<TuffGolemEntity> brain) {
        brain.addActivityWithConditions(Activity.IDLE,
            ImmutableList.of(
                Pair.of(0, new GoToDroppedItem<>((tuffGolem) -> true, 1.15F, true, 10)),
                Pair.of(1, new StayCloseToTarget<>(TuffGolemAi::getItemFramePosition, 18, 30, 1.15F)),
                Pair.of(0, new TuffGolemStack(ModEntities.TUFF_GOLEM.get(), 1.15F)),
                Pair.of(1, new TuffGolemFollowTemptation((entity) -> 1.15F)),
                Pair.of(2, new RunSometimes<>(new TuffGolemLookTarget((entity) -> true, 6.0F), UniformInt.of(30, 60))),
                Pair.of(3, new RunOne<>(
                            ImmutableList.of(
                            Pair.of(new TuffGolemRandomStroll(1.0F), 2),
                            Pair.of(new TuffGolemWalkTargetFromLookTarget(1.0F, 3), 2),
                            Pair.of(new DoNothing(30, 60), 1))))),
            ImmutableSet.of(
                Pair.of(ModMemoryModules.MID_ANIMATE_OR_PETRIFY.get(), MemoryStatus.VALUE_ABSENT)));
    }

    private static void initTakeItemFromFrameActivity(Brain<TuffGolemEntity> brain) {
        brain.addActivityWithConditions(ModActivities.TAKE_ITEM.get(),
            ImmutableList.of(
                Pair.of(0, new GoAndTakeItemFromFrame<>(TIME_BETWEEN_GOING_TO_ITEM_FRAME, (tuffGolem) -> true, 1.15F, true, 20))),
            ImmutableSet.of(
                Pair.of(MemoryModuleType.TEMPTING_PLAYER, MemoryStatus.VALUE_ABSENT),
                Pair.of(ModMemoryModules.STACK_TARGET.get(), MemoryStatus.VALUE_ABSENT),
                Pair.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_PRESENT),
                Pair.of(ModMemoryModules.GO_TO_ITEM_FRAME_COOLDOWN_TICKS.get(), MemoryStatus.VALUE_ABSENT)));
    }

    public static void initReturnItemToFrameActivity(Brain<TuffGolemEntity> brain) {
        brain.addActivityWithConditions(ModActivities.RETURN_ITEM.get(),
            ImmutableList.of(
                Pair.of(0, new GoAndReturnItemToFrame<>(TIME_BETWEEN_GOING_TO_ITEM_FRAME, (tuffGolem) -> true, 1.15F, true, 30))),
            ImmutableSet.of(
                Pair.of(MemoryModuleType.TEMPTING_PLAYER, MemoryStatus.VALUE_ABSENT),
                Pair.of(ModMemoryModules.STACK_TARGET.get(), MemoryStatus.VALUE_ABSENT),
                Pair.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_PRESENT),
                Pair.of(ModMemoryModules.GO_TO_ITEM_FRAME_COOLDOWN_TICKS.get(), MemoryStatus.VALUE_ABSENT),
                Pair.of(ModMemoryModules.SELECTED_ITEM_FRAME.get(), MemoryStatus.VALUE_PRESENT)
            ));
    }

    private static void initPetrifyOrAnimateActivity(Brain<TuffGolemEntity> brain) {
        brain.addActivityWithConditions(ModActivities.ANIMATE_PETRIFY.get(),
            ImmutableList.of(
                Pair.of(0, new PetrifiedTime(TIME_BETWEEN_ANIMATE_OR_PETRIFY)),
                Pair.of(1, new PetrifyOrAnimate<>(TIME_BETWEEN_ANIMATE_OR_PETRIFY, SoundEvents.GRINDSTONE_USE))),
            ImmutableSet.of(
                Pair.of(MemoryModuleType.TEMPTING_PLAYER, MemoryStatus.VALUE_ABSENT),
                Pair.of(ModMemoryModules.NEAREST_VISIBLE_ITEM_FRAME.get(), MemoryStatus.VALUE_ABSENT),
                Pair.of(ModMemoryModules.ANIMATE_OR_PETRIFY_COOLDOWN_TICKS.get(), MemoryStatus.VALUE_ABSENT)
            ));
    }

    public static void updateActivity(TuffGolemEntity tuffGolem) {
        tuffGolem.getBrain().setActiveActivityToFirstValid(
            ImmutableList.of(
                ModActivities.FORGET.get(),
                ModActivities.RETURN_ITEM.get(),
                ModActivities.TAKE_ITEM.get(),
                ModActivities.ANIMATE_PETRIFY.get(),
                Activity.IDLE));
    }

    public static boolean isIdle(AbstractGolem golem) {
        return golem.getBrain().isActive(Activity.IDLE);
    }

    private static void stopWalking(TuffGolemEntity tuffGolem) {
        tuffGolem.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        tuffGolem.getNavigation().stop();
    }

    private static ItemStack removeOneItemFromItemEntity(ItemEntity itemOnGround) {
        ItemStack itemstack = itemOnGround.getItem();
        ItemStack splitItem = itemstack.split(1);
        if (itemstack.isEmpty()) {
            itemOnGround.discard();
        } else {
            itemOnGround.setItem(itemstack);
        }
        return splitItem;
    }

    public static void pickUpItem(TuffGolemEntity tuffGolem, ItemEntity itemOnGround) {
        stopWalking(tuffGolem);
        ItemStack itemstack;
        if (!itemOnGround.getItem().is(Items.SPAWNER)) {
            tuffGolem.take(itemOnGround, itemOnGround.getItem().getCount());
            itemstack = itemOnGround.getItem();
            itemOnGround.discard();
        } else {
            tuffGolem.take(itemOnGround, 1);
            itemstack = removeOneItemFromItemEntity(itemOnGround);
        }
        boolean equippable = tuffGolem.equipItemIfPossible(itemstack);
        if (!equippable) {
            tuffGolem.setItemInHand(InteractionHand.MAIN_HAND, itemstack);
        }
    }

    private static Optional<PositionTracker> getItemFramePosition(LivingEntity entity) {
        Brain<?> brain = entity.getBrain();
        Optional<BlockPos> positionOptional = brain.getMemory(ModMemoryModules.SELECTED_ITEM_FRAME_POSITION.get());
        Optional<ItemFrame> itemFrameOptional = brain.getMemory(ModMemoryModules.SELECTED_ITEM_FRAME.get());
        if (positionOptional.isPresent() && itemFrameOptional.isPresent()) {
            BlockPos pos = positionOptional.get();
            if (shouldReturnItem(entity, brain, pos)) {
                return Optional.of(new BlockPosTracker(pos));
            }
            brain.eraseMemory(ModMemoryModules.SELECTED_ITEM_FRAME_POSITION.get());
        }
        return getItemFramePositionTracker(entity);
    }

    private static Optional<PositionTracker> getItemFramePositionTracker(LivingEntity entity) {
        return getSelectedItemFrame(entity).map((itemFrame) -> new EntityTracker(itemFrame, true));
    }

    public static Optional<ItemFrame> getSelectedItemFrame(LivingEntity livingEntity) {
        Level level = livingEntity.getLevel();
        if (!level.isClientSide() && level instanceof ServerLevel) {
            return livingEntity.getBrain().getMemory(ModMemoryModules.SELECTED_ITEM_FRAME.get());
        }
        return Optional.empty();
    }

    private static boolean shouldReturnItem(LivingEntity entity, Brain<?> brain, BlockPos pos) {
        Optional<Integer> optional = brain.getMemory(ModMemoryModules.GO_TO_ITEM_FRAME_COOLDOWN_TICKS.get());
        Level level = entity.getLevel();
        return level.dimension() == entity.getLevel().dimension() && optional.isEmpty();
    }

    public static Ingredient getTemptations() {
        return Ingredient.of(Items.COPPER_INGOT);
    }
}
