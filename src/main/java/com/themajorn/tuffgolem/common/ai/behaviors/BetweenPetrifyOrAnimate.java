package com.themajorn.tuffgolem.common.ai.behaviors;

import com.google.common.collect.ImmutableMap;
import com.themajorn.tuffgolem.common.entities.TuffGolemEntity;
import com.themajorn.tuffgolem.core.registry.ModMemoryModules;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class BetweenPetrifyOrAnimate extends Behavior<TuffGolemEntity> {
    public static final int TIME_OUT_DURATION = 100;
    private final UniformInt timeBetweenAnimateAndPetrify;
    private SoundEvent petrifySound;

    public BetweenPetrifyOrAnimate(UniformInt betweenPetrify, SoundEvent soundEvent) {
        super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED, MemoryModuleType.LONG_JUMP_MID_JUMP, MemoryStatus.VALUE_PRESENT), 100);
        this.timeBetweenAnimateAndPetrify = betweenPetrify;
        this.petrifySound = soundEvent;
    }

    protected boolean canStillUse(ServerLevel serverLevel, TuffGolemEntity mob, long l) {
        return mob.isOnGround() && !mob.isInWater() && !mob.isInLava();
    }

    protected void start(ServerLevel serverLevel, TuffGolemEntity mob, long l) {
        if (mob.isPetrified()) {
            mob.animate();
        } else {
            mob.petrify();
        }
        mob.playSound(petrifySound);
    }

    protected void stop(ServerLevel serverLevel, TuffGolemEntity mob, long l) {
        mob.getBrain().eraseMemory(ModMemoryModules.MID_ANIMATE_OR_PETRIFY.get());
        mob.getBrain().setMemory(ModMemoryModules.ANIMATE_OR_PETRIFY_COOLDOWN_TICKS.get(), this.timeBetweenAnimateAndPetrify.sample(serverLevel.random));
    }
}