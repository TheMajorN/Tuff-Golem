package com.themajorn.tuffgolem.common.ai;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import com.themajorn.tuffgolem.common.entities.TuffGolemEntity;
import com.themajorn.tuffgolem.common.behaviors.GoAndPlaceItemInFrame;
import com.themajorn.tuffgolem.common.behaviors.GoToDroppedItem;
import com.themajorn.tuffgolem.common.behaviors.PetrifyOrAnimate;
import com.themajorn.tuffgolem.core.registry.ModActivities;
import com.themajorn.tuffgolem.core.registry.ModMemoryModules;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import java.util.Optional;
import java.util.UUID;

public class TuffGolemAi {

    private static final UniformInt TIME_BETWEEN_ANIMATE_OR_PETRIFY = UniformInt.of(600, 1200);

    public static Brain<?> makeBrain(Brain<TuffGolemEntity> brain) {
        initCoreActivity(brain);
        initIdleActivity(brain);
        initPetrifyOrAnimateActivity(brain);
        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.useDefaultActivity();
        return brain;
    }

    private static void initCoreActivity(Brain<TuffGolemEntity> brain) {
        brain.addActivity(Activity.CORE, 0, ImmutableList.of(
                new LookAtTargetSink(45, 90),
                new MoveToTargetSink(),
                new CountDownCooldownTicks(ModMemoryModules.ANIMATE_OR_PETRIFY_COOLDOWN_TICKS.get())
                ));
    }

    private static void initIdleActivity(Brain<TuffGolemEntity> brain) {
        brain.addActivityWithConditions(Activity.IDLE,
                ImmutableList.of(Pair.of(0, new GoToDroppedItem<>((tuffGolem) -> { return true; }, 1.0F, true, 5)),
                        Pair.of(1, new GoAndPlaceItemInFrame<>(TuffGolemAi::getItemPlacePosition, 2.25F)),
                        Pair.of(2, new StayCloseToTarget<>(TuffGolemAi::getItemPlacePosition, 4, 16, 2.25F)),
                        Pair.of(3, new RunSometimes<>(new SetEntityLookTarget((p_218434_) -> { return true; }, 6.0F), UniformInt.of(30, 60))),
                        Pair.of(4, new RunOne<>(ImmutableList.of(Pair.of(new RandomStroll(1.0F), 2),
                                        Pair.of(new SetWalkTargetFromLookTarget(1.0F, 3), 2),
                                        Pair.of(new DoNothing(30, 60), 1))))),
                ImmutableSet.of());
    }

    private static void initPetrifyOrAnimateActivity(Brain<TuffGolemEntity> brain) {
        brain.addActivityWithConditions(ModActivities.ANIMATE.get(),
                ImmutableList.of(Pair.of(0, new PetrifyOrAnimate<>(TIME_BETWEEN_ANIMATE_OR_PETRIFY, SoundEvents.GRINDSTONE_USE))),

                ImmutableSet.of(Pair.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT),
                                Pair.of(ModMemoryModules.ANIMATE_OR_PETRIFY_COOLDOWN_TICKS.get(), MemoryStatus.VALUE_ABSENT)));
    }

    public static void updateActivity(TuffGolemEntity tuffGolem) {
        tuffGolem.getBrain().setActiveActivityToFirstValid(ImmutableList.of(ModActivities.ANIMATE.get(), Activity.IDLE));
    }

    protected static boolean isIdle(AbstractGolem golem) {
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

    public static void pickOutItemFromFrame(TuffGolemEntity tuffGolem, ItemFrame itemFrame) {
        ItemStack itemInFrame = itemFrame.getItem();
        stopWalking(tuffGolem);

        itemFrame.setItem(ItemStack.EMPTY);

        boolean equippable = tuffGolem.equipItemIfPossible(itemInFrame);
        if (!equippable) {
            tuffGolem.setItemInHand(InteractionHand.MAIN_HAND, itemInFrame);
        }
    }

    private static Optional<PositionTracker> getItemPlacePosition(LivingEntity entity) {
        Brain<?> brain = entity.getBrain();
        Optional<GlobalPos> optional = brain.getMemory(ModMemoryModules.SELECTED_ITEM_FRAME_POSITION.get());
        if (optional.isPresent()) {
            GlobalPos globalpos = optional.get();
            if (shouldDepositItemAtSelectedFrame(entity, brain, globalpos)) {
                return Optional.of(new BlockPosTracker(globalpos.pos().above()));
            }

            brain.eraseMemory(ModMemoryModules.SELECTED_ITEM_FRAME_POSITION.get());
        }

        return getItemFramePositionTracker(entity);
    }

    private static Optional<PositionTracker> getItemFramePositionTracker(LivingEntity entity) {
        return getSelectedItemFrame(entity).map((frame) -> {
            return new EntityTracker(frame, true);
        });
    }

    public static Optional<ItemFrame> getSelectedItemFrame(LivingEntity livingEntity) {
        Level level = livingEntity.getLevel();
        if (!level.isClientSide() && level instanceof ServerLevel serverlevel) {
            Optional<UUID> optional = livingEntity.getBrain().getMemory(ModMemoryModules.SELECTED_ITEM_FRAME.get());
            if (optional.isPresent()) {
                Entity entity = serverlevel.getEntity(optional.get());
                if (entity instanceof ItemFrame itemFrame) {
                    return Optional.of(itemFrame);
                }
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    private static boolean shouldDepositItemAtSelectedFrame(LivingEntity entity, Brain<?> brain, GlobalPos globalPos) {
        Optional<Integer> optional = brain.getMemory(ModMemoryModules.ITEM_FRAME_COOLDOWN_TICKS.get());
        Level level = entity.getLevel();
        return level.dimension() == globalPos.dimension() && level.getBlockState(globalPos.pos()).is(Blocks.AIR) && optional.isPresent();
    }
}
