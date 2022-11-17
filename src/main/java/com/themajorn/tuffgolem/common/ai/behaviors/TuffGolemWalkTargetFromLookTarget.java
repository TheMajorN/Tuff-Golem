package com.themajorn.tuffgolem.common.ai.behaviors;

import com.google.common.collect.ImmutableMap;
import com.themajorn.tuffgolem.common.entities.TuffGolemEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;

import java.util.function.Function;
import java.util.function.Predicate;

public class TuffGolemWalkTargetFromLookTarget extends Behavior<TuffGolemEntity> {
    private final Function<LivingEntity, Float> speedModifier;
    private final int closeEnoughDistance;
    private final Predicate<LivingEntity> canSetWalkTargetPredicate;

    public TuffGolemWalkTargetFromLookTarget(float p_24084_, int p_24085_) {
        this((p_182369_) -> {
            return true;
        }, (p_182364_) -> {
            return p_24084_;
        }, p_24085_);
    }

    public TuffGolemWalkTargetFromLookTarget(Predicate<LivingEntity> p_182359_, Function<LivingEntity, Float> p_182360_, int p_182361_) {
        super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT, MemoryModuleType.LOOK_TARGET, MemoryStatus.VALUE_PRESENT));
        this.speedModifier = p_182360_;
        this.closeEnoughDistance = p_182361_;
        this.canSetWalkTargetPredicate = p_182359_;
    }

    protected boolean checkExtraStartConditions(ServerLevel serverLevel, TuffGolemEntity tuffGolem) {
        return this.canSetWalkTargetPredicate.test(tuffGolem) && !tuffGolem.isPetrified();
    }

    protected void start(ServerLevel serverLevel, TuffGolemEntity tuffGolem, long l) {
        Brain<?> brain = tuffGolem.getBrain();
        PositionTracker positiontracker = brain.getMemory(MemoryModuleType.LOOK_TARGET).get();
        brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(positiontracker, this.speedModifier.apply(tuffGolem), this.closeEnoughDistance));
    }
}