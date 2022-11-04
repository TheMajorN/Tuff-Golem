package com.themajorn.tuffgolem.common.behaviors;

import com.google.common.collect.ImmutableMap;
import com.themajorn.tuffgolem.TuffGolem;
import com.themajorn.tuffgolem.common.entities.TuffGolemEntity;
import com.themajorn.tuffgolem.core.registry.ModMemoryModules;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import org.jetbrains.annotations.NotNull;

public class PetrifyOrAnimate<E extends Mob> extends Behavior<TuffGolemEntity> {

    private final UniformInt timeBetweenAnimateOrPetrify;
    private final SoundEvent getAnimateOrPetrifySound;


    public PetrifyOrAnimate(UniformInt uniformInt, SoundEvent soundEvent) {
        super(ImmutableMap.of(
                ModMemoryModules.ANIMATE_OR_PETRIFY_COOLDOWN_TICKS.get(), MemoryStatus.VALUE_ABSENT), 200);
        this.timeBetweenAnimateOrPetrify = uniformInt;
        this.getAnimateOrPetrifySound = soundEvent;
    }

    protected boolean checkExtraStartConditions(@NotNull ServerLevel serverLevel, TuffGolemEntity mob) {
        boolean isValidPosition = mob.isOnGround() && !mob.isInWater() && !mob.isInLava();
        if (!isValidPosition) {
            mob.getBrain().setMemory(ModMemoryModules.ANIMATE_OR_PETRIFY_COOLDOWN_TICKS.get(), this.timeBetweenAnimateOrPetrify.sample(serverLevel.random) / 2);
        }
        return isValidPosition;
    }

    protected boolean canStillUse(@NotNull ServerLevel serverLevel, TuffGolemEntity mob, long i) {
        boolean validMorphConditions = !mob.isInWaterOrBubble() || !mob.isSwimming();
        if (!validMorphConditions) {
            mob.getBrain().setMemory(ModMemoryModules.ANIMATE_OR_PETRIFY_COOLDOWN_TICKS.get(), this.timeBetweenAnimateOrPetrify.sample(serverLevel.random) / 2);
        }
        return validMorphConditions;
    }

    protected void start(@NotNull ServerLevel level, @NotNull TuffGolemEntity tuffGolem, long l) {
        if (tuffGolem.isPetrified()) {
            tuffGolem.animate();
            tuffGolem.isPetrified = false;
            tuffGolem.isAnimated = true;
            TuffGolem.LOGGER.info("Tuff Golem Animated!");
        } else {
            tuffGolem.petrify();
            tuffGolem.isAnimated = false;
            tuffGolem.isPetrified = true;
            TuffGolem.LOGGER.info("Tuff Golem Petrified!");
        }
        tuffGolem.playSound(getAnimateOrPetrifySound);
    }

    protected void stop(@NotNull ServerLevel level, TuffGolemEntity mob, long i) {
        if (!mob.isOnGround() || mob.isAnimating() || mob.isPetrifying()) {
            TuffGolem.LOGGER.info("Stopping! (PoA)");
        }

        mob.getBrain().setMemory(ModMemoryModules.ANIMATE_OR_PETRIFY_COOLDOWN_TICKS.get(), this.timeBetweenAnimateOrPetrify.sample(level.random));
    }
}
