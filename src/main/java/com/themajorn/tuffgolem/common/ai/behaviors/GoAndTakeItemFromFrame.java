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

public class GoAndTakeItemFromFrame<E extends LivingEntity> extends Behavior<TuffGolemEntity> {
    private final UniformInt timeBetweenGoToItemFrame;
    private final Predicate<E> predicate;
    private final int maxDistToWalk;
    private final float speedModifier;


    public GoAndTakeItemFromFrame(UniformInt cooldownTicks, Predicate<E> predicate, float speedMod, boolean memStatus, int interestRadius) {
        super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED,
                MemoryModuleType.WALK_TARGET, memStatus ? MemoryStatus.REGISTERED : MemoryStatus.VALUE_ABSENT,
                ModMemoryModules.NEAREST_VISIBLE_ITEM_FRAME.get(), MemoryStatus.VALUE_PRESENT));
        this.timeBetweenGoToItemFrame = cooldownTicks;
        this.predicate = predicate;
        this.maxDistToWalk = interestRadius;
        this.speedModifier = speedMod;
    }

    protected boolean checkExtraStartConditions(ServerLevel serverLevel, TuffGolemEntity entity) {
        boolean validStartConditions = !this.isOnGoToCooldown(entity)
                && this.getClosestItemFrame(entity).closerThan(entity, (double)this.maxDistToWalk)
                && !getClosestItemFrame(entity).getItem().isEmpty()
                && entity.hasCloak()
                && !entity.hasItemInHand()
                && !entity.isPetrified();
        if (!validStartConditions) {
            entity.getBrain().setMemory(ModMemoryModules.GO_TO_ITEM_FRAME_COOLDOWN_TICKS.get(), this.timeBetweenGoToItemFrame.sample(serverLevel.random) / 2);
        }
        return validStartConditions;
    }

    protected void start(ServerLevel serverLevel, TuffGolemEntity entity, long l) {
        BehaviorUtils.setWalkAndLookTargetMemories(entity, this.getClosestItemFrame(entity), this.speedModifier, 0);
        if (entity.getBrain().checkMemory(ModMemoryModules.SELECTED_ITEM_FRAME.get(), MemoryStatus.VALUE_ABSENT)) {
            entity.getBrain().setMemory(ModMemoryModules.SELECTED_ITEM_FRAME.get(), getClosestItemFrame(entity));
        }
        TuffGolem.LOGGER.info("Picking out item!");
        entity.pickOutItem();
    }

    protected boolean canStillUse(ServerLevel serverLevel, TuffGolemEntity entity, long l) {
        boolean validContinueConditions = !getClosestItemFrame(entity).getItem().isEmpty() && !entity.isPetrified();
        if (!validContinueConditions) {
            entity.getBrain().setMemory(ModMemoryModules.GO_TO_ITEM_FRAME_COOLDOWN_TICKS.get(), this.timeBetweenGoToItemFrame.sample(serverLevel.random) / 2);
        }
        return validContinueConditions;
    }

    private boolean isOnGoToCooldown(TuffGolemEntity entity) {
        return entity.getBrain().checkMemory(ModMemoryModules.GO_TO_ITEM_FRAME_COOLDOWN_TICKS.get(), MemoryStatus.VALUE_PRESENT);
    }

    private ItemFrame getClosestItemFrame(TuffGolemEntity entity) {
        return entity.getBrain().getMemory(ModMemoryModules.NEAREST_VISIBLE_ITEM_FRAME.get()).get();
    }

}
