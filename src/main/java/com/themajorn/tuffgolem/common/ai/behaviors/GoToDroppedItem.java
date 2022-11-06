package com.themajorn.tuffgolem.common.ai.behaviors;

import com.google.common.collect.ImmutableMap;
import com.themajorn.tuffgolem.common.entities.TuffGolemEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.item.ItemEntity;

import java.util.function.Predicate;

public class GoToDroppedItem<E extends LivingEntity> extends Behavior<TuffGolemEntity> {
    private final Predicate<E> predicate;
    private final int maxDistToWalk;
    private final float speedModifier;

    public GoToDroppedItem(Predicate<E> predicate, float speedMod, boolean memStatus, int interestRadius) {
        super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED,
                MemoryModuleType.WALK_TARGET, memStatus ? MemoryStatus.REGISTERED : MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM, MemoryStatus.VALUE_PRESENT));
        this.predicate = predicate;
        this.maxDistToWalk = interestRadius;
        this.speedModifier = speedMod;
    }

    protected boolean checkExtraStartConditions(ServerLevel serverLevel, TuffGolemEntity entity) {
        return !this.isOnPickupCooldown(entity)
                && this.predicate.test((E) entity)
                && this.getClosestDroppedItem(entity).closerThan(entity, this.maxDistToWalk)
                && entity.hasCloak()
                && !entity.hasItemInHand()
                && !entity.isPetrified();
    }

    protected void start(ServerLevel serverLevel, TuffGolemEntity entity, long l) {
            BehaviorUtils.setWalkAndLookTargetMemories(entity, this.getClosestDroppedItem(entity), this.speedModifier, 0);
    }

    private boolean isOnPickupCooldown(TuffGolemEntity entity) {
        return entity.getBrain().checkMemory(MemoryModuleType.ITEM_PICKUP_COOLDOWN_TICKS, MemoryStatus.VALUE_PRESENT);
    }

    private ItemEntity getClosestDroppedItem(TuffGolemEntity entity) {
        return entity.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM).get();
    }
}
