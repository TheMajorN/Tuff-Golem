package com.themajorn.tuffgolem.common.ai.behaviors;

import com.google.common.collect.ImmutableMap;
import com.themajorn.tuffgolem.common.entities.TuffGolemEntity;
import com.themajorn.tuffgolem.core.registry.ModMemoryModules;
import net.minecraft.Util;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;
import java.util.function.Function;

public class TuffGolemFollowTemptation extends Behavior<TuffGolemEntity> {
    public static final int TEMPTATION_COOLDOWN = 100;
    public static final double CLOSE_ENOUGH_DIST = 2.5D;
    private final Function<LivingEntity, Float> speedModifier;

    public TuffGolemFollowTemptation(Function<LivingEntity, Float> entityFloatFunction) {
        super(Util.make(() -> {
            ImmutableMap.Builder<MemoryModuleType<?>, MemoryStatus> builder = ImmutableMap.builder();
            builder.put(MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED);
            builder.put(MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED);
            builder.put(MemoryModuleType.TEMPTATION_COOLDOWN_TICKS, MemoryStatus.VALUE_ABSENT);
            builder.put(MemoryModuleType.IS_TEMPTED, MemoryStatus.REGISTERED);
            builder.put(MemoryModuleType.TEMPTING_PLAYER, MemoryStatus.VALUE_PRESENT);
            builder.put(ModMemoryModules.STACK_TARGET.get(), MemoryStatus.VALUE_ABSENT);
            return builder.build();
        }));
        this.speedModifier = entityFloatFunction;
    }

    protected float getSpeedModifier(TuffGolemEntity tuffGolem) {
        return this.speedModifier.apply(tuffGolem);
    }

    private Optional<Player> getTemptingPlayer(TuffGolemEntity tuffGolem) {
        return tuffGolem.getBrain().getMemory(MemoryModuleType.TEMPTING_PLAYER);
    }

    protected boolean timedOut(long l) {
        return false;
    }

    protected boolean canStillUse(ServerLevel serverLevel, TuffGolemEntity tuffGolem, long l) {
        return this.getTemptingPlayer(tuffGolem).isPresent() && !tuffGolem.getBrain().hasMemoryValue(ModMemoryModules.STACK_TARGET.get());
    }

    protected void start(ServerLevel serverLevel, TuffGolemEntity tuffGolem, long l) {
        tuffGolem.getBrain().setMemory(MemoryModuleType.IS_TEMPTED, true);
    }

    protected void stop(ServerLevel serverLevel, TuffGolemEntity tuffGolem, long l) {
        Brain<?> brain = tuffGolem.getBrain();
        brain.setMemory(MemoryModuleType.TEMPTATION_COOLDOWN_TICKS, 100);
        brain.setMemory(MemoryModuleType.IS_TEMPTED, false);
        brain.eraseMemory(MemoryModuleType.WALK_TARGET);
        brain.eraseMemory(MemoryModuleType.LOOK_TARGET);
    }

    protected void tick(ServerLevel serverLevel, TuffGolemEntity tuffGolem, long l) {
        Player player = this.getTemptingPlayer(tuffGolem).get();
        Brain<?> brain = tuffGolem.getBrain();
        brain.setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(player, true));
        if (tuffGolem.distanceToSqr(player) < 6.25D) {
            brain.eraseMemory(MemoryModuleType.WALK_TARGET);
        } else {
            brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(new EntityTracker(player, false), this.getSpeedModifier(tuffGolem), 2));
        }

    }
}
