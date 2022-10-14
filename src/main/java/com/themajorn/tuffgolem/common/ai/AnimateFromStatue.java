package com.themajorn.tuffgolem.common.ai;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.themajorn.tuffgolem.core.registry.ModMemoryModules;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class AnimateFromStatue<E extends Mob> extends Behavior<E> {

    private final UniformInt timeBetweenAnimation;
    protected Optional<Vec3> initialPosition = Optional.empty();
    private Function<E, SoundEvent> getAnimateSound;

    public AnimateFromStatue(UniformInt unInt1, int i, int j, float f, Function<E, SoundEvent> soundEvent) {
        this(unInt1, i, j, f, soundEvent, (blockState) -> {
            return false;
        });
    }

    public AnimateFromStatue(UniformInt unInt1, int i, int t, float j, Function<E, SoundEvent> soundEvent, Predicate<BlockState> predicate) {
        super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED, ModMemoryModules.ANIMATE_FROM_STATUE_TICKS, MemoryStatus.VALUE_ABSENT, ModMemoryModules.ANIMATE_MID_ANIMATE, MemoryStatus.VALUE_ABSENT), 200);
        this.timeBetweenAnimation = unInt1;
    }

    protected boolean checkExtraStartConditions(ServerLevel serverLevel, Mob mob) {
        boolean conditionsMet = mob.isOnGround() && !mob.isInWater() && !mob.isInLava();
        if (!conditionsMet) {
            mob.getBrain().setMemory(ModMemoryModules.ANIMATE_FROM_STATUE_TICKS, this.timeBetweenAnimation.sample(serverLevel.random) / 2);
        }

        return conditionsMet;
    }

    protected boolean canStillUse(ServerLevel serverLevel, Mob mob, long l) {
        boolean conditionsMet = this.initialPosition.isPresent() && this.initialPosition.get().equals(mob.position()) && !mob.isInWaterOrBubble();
        if (!conditionsMet && mob.getBrain().getMemory(ModMemoryModules.ANIMATE_MID_ANIMATE).isEmpty()) {
            mob.getBrain().setMemory(ModMemoryModules.ANIMATE_FROM_STATUE_TICKS, this.timeBetweenAnimation.sample(serverLevel.random) / 2);
        }
        return conditionsMet;
    }

    protected void start(ServerLevel serverLevel, E e, long l) {
        this.initialPosition = Optional.of(e.position());
        BlockPos blockpos = e.blockPosition();
        int i = blockpos.getX();
        int j = blockpos.getY();
        int k = blockpos.getZ();
    }

    protected void tick(ServerLevel serverLevel, E e, long l) {
                e.setDiscardFriction(true);
                e.getBrain().setMemory(ModMemoryModules.ANIMATE_MID_ANIMATE, true);
                serverLevel.playSound((Player)null, e, this.getAnimateSound.apply(e), SoundSource.NEUTRAL, 1.0F, 1.0F);
    }
}
