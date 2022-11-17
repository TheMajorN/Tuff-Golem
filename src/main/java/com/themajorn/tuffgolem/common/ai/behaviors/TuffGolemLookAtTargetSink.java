package com.themajorn.tuffgolem.common.ai.behaviors;

import com.google.common.collect.ImmutableMap;
import com.themajorn.tuffgolem.common.entities.TuffGolemEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class TuffGolemLookAtTargetSink extends Behavior<TuffGolemEntity> {
    public TuffGolemLookAtTargetSink(int p_23478_, int p_23479_) {
        super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryStatus.VALUE_PRESENT), p_23478_, p_23479_);
    }

    protected boolean checkExtraStartConditions(ServerLevel serverLevel, TuffGolemEntity tuffGolem) {
        return !tuffGolem.isPetrified();
    }

    protected boolean canStillUse(ServerLevel p_23481_, TuffGolemEntity tuffGolem, long p_23483_) {
        return !tuffGolem.isPetrified() && tuffGolem.getBrain().getMemory(MemoryModuleType.LOOK_TARGET).filter((tracker) -> tracker.isVisibleBy(tuffGolem)).isPresent();
    }

    protected void stop(ServerLevel p_23492_, TuffGolemEntity p_23493_, long p_23494_) {
        p_23493_.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
    }

    protected void tick(ServerLevel p_23503_, TuffGolemEntity p_23504_, long p_23505_) {
        p_23504_.getBrain().getMemory(MemoryModuleType.LOOK_TARGET).ifPresent((p_23486_) -> {
            p_23504_.getLookControl().setLookAt(p_23486_.currentPosition());
        });
    }
}
