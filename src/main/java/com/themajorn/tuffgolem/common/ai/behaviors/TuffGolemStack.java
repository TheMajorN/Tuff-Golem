package com.themajorn.tuffgolem.common.ai.behaviors;

import com.google.common.collect.ImmutableMap;
import com.themajorn.tuffgolem.TuffGolem;
import com.themajorn.tuffgolem.common.entities.TuffGolemEntity;
import com.themajorn.tuffgolem.core.registry.ModMemoryModules;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.monster.Spider;

import java.util.Optional;

public class TuffGolemStack extends Behavior<TuffGolemEntity> {
    private static final int BREED_RANGE = 3;
    private static final int MIN_DURATION = 60;
    private static final int MAX_DURATION = 110;
    private final EntityType<? extends TuffGolemEntity> partnerType;
    private final float speedModifier;
    private long stackAtTime;

    public TuffGolemStack(EntityType<? extends TuffGolemEntity> entityType, float speedMod) {
        super(ImmutableMap.of(
                MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryStatus.VALUE_PRESENT,
                ModMemoryModules.STACK_TARGET.get(), MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED,
                MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED), 110);
        this.partnerType = entityType;
        this.speedModifier = speedMod;
    }

    protected boolean checkExtraStartConditions(ServerLevel serverLevel, TuffGolemEntity tuffGolem) {
        return  tuffGolem.wantsToStack()
                && !tuffGolem.isPetrified()
                && this.findValidStackPartner(tuffGolem).isPresent()
                && tuffGolem.getBottomTuffGolem(tuffGolem).getNumOfTuffGolemsAbove(tuffGolem, 1) < tuffGolem.getMaxStackSize();
    }

    protected void start(ServerLevel serverLevel, TuffGolemEntity tuffGolem, long l) {
        TuffGolemEntity stackTarget = this.findValidStackPartner(tuffGolem).get();
        tuffGolem.getBrain().setMemory(ModMemoryModules.STACK_TARGET.get(), stackTarget);
        stackTarget.getBrain().setMemory(ModMemoryModules.STACK_TARGET.get(), tuffGolem);
        BehaviorUtils.lockGazeAndWalkToEachOther(tuffGolem, stackTarget, this.speedModifier);
        int i = 60 + tuffGolem.getRandom().nextInt(50);
        this.stackAtTime = l + (long) i;
    }

    protected boolean canStillUse(ServerLevel serverLevel, TuffGolemEntity tuffGolem, long l) {
        if (!this.hasStackTargetOfRightType(tuffGolem)) {
            return false;
        } else {
            TuffGolemEntity stackTarget = this.getStackTarget(tuffGolem);
            return stackTarget.isAlive() && !tuffGolem.isPetrified() && BehaviorUtils.entityIsVisible(tuffGolem.getBrain(), stackTarget) && l <= this.stackAtTime;
        }
    }

    protected void tick(ServerLevel serverLevel, TuffGolemEntity tuffGolem, long l) {
        TuffGolemEntity stackTarget = this.getStackTarget(tuffGolem);
        BehaviorUtils.lockGazeAndWalkToEachOther(tuffGolem, stackTarget, this.speedModifier);
        if (tuffGolem.closerThan(stackTarget, 2.0D)) {
            if (l >= this.stackAtTime) {
                tuffGolem.startRiding(stackTarget);
                tuffGolem.setYRot(stackTarget.getYRot());
                tuffGolem.setHeightDimensionState(1);
                tuffGolem.setWidthDimensionState(1);
                tuffGolem.setPassengersRidingOffset(0.9D);
                stackTarget.setHeightDimensionState(stackTarget.getNumOfTuffGolemsAbove(stackTarget, 1));
                stackTarget.setWidthDimensionState(2);
                stackTarget.setPassengersRidingOffset(stackTarget.getNumOfTuffGolemsAbove(stackTarget, 1) - 0.1);
                TuffGolem.LOGGER.info("There are " + stackTarget.getNumOfTuffGolemsAbove(stackTarget, 1) + " Tuff Golems stacked.");
                tuffGolem.getBrain().eraseMemory(ModMemoryModules.STACK_TARGET.get());
                stackTarget.getBrain().eraseMemory(ModMemoryModules.STACK_TARGET.get());
            }
        }
    }

    protected void stop(ServerLevel serverLevel, TuffGolemEntity tuffGolem, long l) {
        tuffGolem.getBrain().eraseMemory(ModMemoryModules.STACK_TARGET.get());
        tuffGolem.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        tuffGolem.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
        tuffGolem.resetWantsToStack();
        this.stackAtTime = 0L;
    }

    private TuffGolemEntity getStackTarget(TuffGolemEntity tuffGolem) {
        return tuffGolem.getBrain().getMemory(ModMemoryModules.STACK_TARGET.get()).get();
    }

    private boolean hasStackTargetOfRightType(TuffGolemEntity tuffGolem) {
        Brain<?> brain = tuffGolem.getBrain();
        return brain.hasMemoryValue(ModMemoryModules.STACK_TARGET.get()) && brain.getMemory(ModMemoryModules.STACK_TARGET.get()).get().getType() == this.partnerType;
    }

    private Optional<? extends TuffGolemEntity> findValidStackPartner(TuffGolemEntity tuffGolem) {
        return tuffGolem.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).get().findClosest((entity) -> {
            if (entity.getType() == this.partnerType && entity instanceof TuffGolemEntity) {
                return !tuffGolem.isPetrified();
            }
            return false;
        }).map(TuffGolemEntity.class::cast);
    }
}