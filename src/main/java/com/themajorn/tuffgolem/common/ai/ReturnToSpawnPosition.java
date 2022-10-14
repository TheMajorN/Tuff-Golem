package com.themajorn.tuffgolem.common.ai;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.npc.InventoryCarrier;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class ReturnToSpawnPosition<E extends LivingEntity & InventoryCarrier> extends Behavior<E> {

    private static final int CLOSE_ENOUGH_DISTANCE_TO_TARGET = 3;
    private static final int ITEM_PICKUP_COOLDOWN_AFTER_THROWING = 60;
    private final Function<LivingEntity, Optional<PositionTracker>> targetPositionGetter;
    private final float speedModifier;

    public ReturnToSpawnPosition(Function<LivingEntity, Optional<PositionTracker>> p_217193_, float speedMod) {
        super(Map.of(MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED, MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED, MemoryModuleType.ITEM_PICKUP_COOLDOWN_TICKS, MemoryStatus.REGISTERED));
        this.targetPositionGetter = p_217193_;
        this.speedModifier = speedMod;
    }

    protected boolean checkExtraStartConditions(@NotNull ServerLevel serverLevel, @NotNull E e) {
        return this.canReturnToTarget(e);
    }

    protected boolean canStillUse(@NotNull ServerLevel serverLevel, @NotNull E e, long l) {
        return this.canReturnToTarget(e);
    }

    protected void start(@NotNull ServerLevel serverLevel, @NotNull E e, long l) {
        this.targetPositionGetter.apply(e).ifPresent((tracker) -> {
            BehaviorUtils.setWalkAndLookTargetMemories(e, tracker, this.speedModifier, 3);
        });
    }

    protected void tick(@NotNull ServerLevel serverLevel, @NotNull E e, long l) {
        Optional<PositionTracker> optional = this.targetPositionGetter.apply(e);
        if (!optional.isEmpty()) {
            PositionTracker positiontracker = optional.get();
            double d0 = positiontracker.currentPosition().distanceTo(e.getEyePosition());
        }
    }

    private boolean canReturnToTarget(E tuffGolem) {
        Optional<PositionTracker> optional = this.targetPositionGetter.apply(tuffGolem);
        return optional.isPresent();
    }

}
