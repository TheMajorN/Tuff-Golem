package com.themajorn.tuffgolem.common.ai;

import com.google.common.collect.ImmutableMap;
import com.themajorn.tuffgolem.core.registry.ModMemoryModules;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public class AnimateMidAnimation extends Behavior<Mob> {

    public static final int TIME_OUT_DURATION = 100;
    private final UniformInt timeBetweenReanimation;
    private SoundEvent animateSound;

    public AnimateMidAnimation(UniformInt unInt, SoundEvent soundEvent) {
        super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED, MemoryModuleType.LONG_JUMP_MID_JUMP, MemoryStatus.VALUE_PRESENT), 100);
        this.timeBetweenReanimation = unInt;
        this.animateSound = soundEvent;
    }

    protected boolean canStillUse(@NotNull ServerLevel serverLevel, Mob mob, long l) {
        return !mob.isOnGround();
    }

    protected void start(@NotNull ServerLevel serverLevel, Mob mob, long l) {
        mob.setDiscardFriction(true);
        mob.setPose(Pose.LONG_JUMPING);
    }

    protected void stop(@NotNull ServerLevel serverLevel, Mob mob, long l) {
        if (mob.isOnGround()) {
            mob.setDeltaMovement(mob.getDeltaMovement().multiply((double)0.1F, 1.0D, (double)0.1F));
            serverLevel.playSound((Player)null, mob, this.animateSound, SoundSource.NEUTRAL, 2.0F, 1.0F);
        }

        mob.setDiscardFriction(false);
        mob.setPose(Pose.STANDING);
        mob.getBrain().eraseMemory(ModMemoryModules.ANIMATE_MID_ANIMATE);
        mob.getBrain().setMemory(ModMemoryModules.ANIMATE_FROM_STATUE_TICKS, this.timeBetweenReanimation.sample(serverLevel.random));
    }

}
