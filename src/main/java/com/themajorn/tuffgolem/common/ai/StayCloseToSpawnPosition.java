package com.themajorn.tuffgolem.common.ai;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class StayCloseToSpawnPosition<E extends LivingEntity> extends Behavior<E> {
    private final Function<LivingEntity, Optional<PositionTracker>> targetPositionGetter;
    private final int closeEnough;
    private final int tooFar;
    private final float speedModifier;

    public StayCloseToSpawnPosition(Function<LivingEntity, Optional<PositionTracker>> positionTracker, int radius, int tooFarRadius, float speedMod) {
        super(Map.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT));
        this.targetPositionGetter = positionTracker;
        this.closeEnough = radius;
        this.tooFar = tooFarRadius;
        this.speedModifier = speedMod;
    }

    protected boolean checkExtraStartConditions(ServerLevel serverLevel, E e) {
        Optional<PositionTracker> optional = this.targetPositionGetter.apply(e);
        if (optional.isEmpty()) {
            return false;
        } else {
            PositionTracker positiontracker = optional.get();
            return !e.position().closerThan(positiontracker.currentPosition(), (double)this.tooFar);
        }
    }

    protected void start(ServerLevel serverLevel, E e, long l) {
        BehaviorUtils.setWalkAndLookTargetMemories(e, this.targetPositionGetter.apply(e).get(), this.speedModifier, this.closeEnough);
    }
}
