package com.themajorn.tuffgolem.common.ai.behaviors;

import com.google.common.collect.ImmutableMap;
import com.themajorn.tuffgolem.common.ai.TuffGolemAi;
import com.themajorn.tuffgolem.common.entities.TuffGolemEntity;
import com.themajorn.tuffgolem.core.registry.ModMemoryModules;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class PetrifiedTime extends Behavior<TuffGolemEntity> {

    private final UniformInt timeBetweenAnimateAndPetrify;

    public PetrifiedTime(UniformInt betweenPetrify) {
        super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED,
                ModMemoryModules.MID_ANIMATE_OR_PETRIFY.get(), MemoryStatus.VALUE_PRESENT), 100);
        this.timeBetweenAnimateAndPetrify = betweenPetrify;
    }

    protected boolean canStillUse(ServerLevel serverLevel, TuffGolemEntity mob, long l) {
        return mob.isOnGround() && !mob.isInWater() && !mob.isInLava() && !mob.stateLocked() && TuffGolemAi.isIdle(mob);
    }

    protected void start(ServerLevel serverLevel, TuffGolemEntity mob, long l) {

    }

    protected void stop(ServerLevel serverLevel, TuffGolemEntity mob, long l) {
        mob.getBrain().eraseMemory(ModMemoryModules.MID_ANIMATE_OR_PETRIFY.get());
        mob.getBrain().setMemory(ModMemoryModules.ANIMATE_OR_PETRIFY_COOLDOWN_TICKS.get(), this.timeBetweenAnimateAndPetrify.sample(serverLevel.random));
    }
}