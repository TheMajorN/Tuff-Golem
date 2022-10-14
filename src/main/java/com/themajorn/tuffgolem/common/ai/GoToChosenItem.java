package com.themajorn.tuffgolem.common.ai;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.item.ItemEntity;

import java.util.function.Predicate;

public class GoToChosenItem<E extends LivingEntity> extends Behavior<E> {

    private final Predicate<E> predicate;
    private final int maxDistToWalk;
    private final float speedModifier;

    public GoToChosenItem(float f, boolean b, int i) {
        this((e) -> {
            return true;
        }, f, b, i);
    }

    public GoToChosenItem(Predicate<E> predicate, float f, boolean b, int i) {
        super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED, MemoryModuleType.WALK_TARGET, b ? MemoryStatus.REGISTERED : MemoryStatus.VALUE_ABSENT, MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM, MemoryStatus.VALUE_PRESENT));
        this.predicate = predicate;
        this.maxDistToWalk = i;
        this.speedModifier = f;
    }

    protected boolean checkExtraStartConditions(ServerLevel serverLevel, E e) {
        return !this.isOnPickupCooldown(e) && this.predicate.test(e) && this.getClosestLovedItem(e).closerThan(e, (double)this.maxDistToWalk);
    }

    protected void start(ServerLevel serverLevel, E e, long l) {
        BehaviorUtils.setWalkAndLookTargetMemories(e, this.getClosestLovedItem(e), this.speedModifier, 0);
    }

    private boolean isOnPickupCooldown(E e) {
        return e.getBrain().checkMemory(MemoryModuleType.ITEM_PICKUP_COOLDOWN_TICKS, MemoryStatus.VALUE_PRESENT);
    }

    private ItemEntity getClosestLovedItem(E e) {
        return e.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM).get();
    }
}
