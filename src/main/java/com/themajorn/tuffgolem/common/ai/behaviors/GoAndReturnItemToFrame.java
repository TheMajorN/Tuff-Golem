package com.themajorn.tuffgolem.common.ai.behaviors;

import com.google.common.collect.ImmutableMap;
import com.themajorn.tuffgolem.TuffGolem;
import com.themajorn.tuffgolem.common.entities.TuffGolemEntity;
import com.themajorn.tuffgolem.core.registry.ModMemoryModules;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.decoration.ItemFrame;

import java.util.function.Predicate;

public class GoAndReturnItemToFrame<E extends LivingEntity> extends Behavior<TuffGolemEntity> {
    private final UniformInt timeBetweenGoToItemFrame;
    private final Predicate<E> predicate;
    private final int maxDistToWalk;
    private final float speedModifier;


    public GoAndReturnItemToFrame(UniformInt cooldownTicks, Predicate<E> predicate, float speedMod, boolean memStatus, int interestRadius) {
        super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED,
                MemoryModuleType.WALK_TARGET, memStatus ? MemoryStatus.REGISTERED : MemoryStatus.VALUE_ABSENT,
                ModMemoryModules.SELECTED_ITEM_FRAME.get(), MemoryStatus.VALUE_PRESENT));
        this.timeBetweenGoToItemFrame = cooldownTicks;
        this.predicate = predicate;
        this.maxDistToWalk = interestRadius;
        this.speedModifier = speedMod;
    }

    protected boolean checkExtraStartConditions(ServerLevel serverLevel, TuffGolemEntity entity) {
        boolean validStartConditions = !this.isOnGoToCooldown(entity)
                && this.getSelectedItemFrame(entity).closerThan(entity, (double)this.maxDistToWalk)
                && getSelectedItemFrame(entity).getItem().isEmpty()
                && entity.hasCloak()
                && entity.hasItemInHand()
                && !entity.isPetrified();
        if (!validStartConditions) {
            entity.getBrain().eraseMemory(ModMemoryModules.SELECTED_ITEM_FRAME.get());
            entity.getBrain().setMemory(ModMemoryModules.GO_TO_ITEM_FRAME_COOLDOWN_TICKS.get(), this.timeBetweenGoToItemFrame.sample(serverLevel.random) / 6);
        }
        return validStartConditions;
    }

    protected void start(ServerLevel serverLevel, TuffGolemEntity entity, long l) {
        BehaviorUtils.setWalkAndLookTargetMemories(entity, this.getSelectedItemFrame(entity), this.speedModifier, 1);
        TuffGolem.LOGGER.info("Attempting to return item!");
        entity.putBackItem();
    }

    protected boolean canStillUse(ServerLevel serverLevel, TuffGolemEntity entity, long l) {
        boolean validContinueConditions = getSelectedItemFrame(entity).getItem().isEmpty() && entity.hasItemInHand();
        if (!validContinueConditions) {
            entity.getBrain().eraseMemory(ModMemoryModules.SELECTED_ITEM_FRAME.get());
            entity.getBrain().setMemory(ModMemoryModules.GO_TO_ITEM_FRAME_COOLDOWN_TICKS.get(), this.timeBetweenGoToItemFrame.sample(serverLevel.random) / 6);
        }
        return validContinueConditions;
    }

    private boolean isOnGoToCooldown(TuffGolemEntity entity) {
        return entity.getBrain().checkMemory(ModMemoryModules.GO_TO_ITEM_FRAME_COOLDOWN_TICKS.get(), MemoryStatus.VALUE_PRESENT);
    }

    private ItemFrame getSelectedItemFrame(TuffGolemEntity entity) {
        return entity.getBrain().getMemory(ModMemoryModules.SELECTED_ITEM_FRAME.get()).get();
    }

}