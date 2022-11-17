package com.themajorn.tuffgolem.common.ai.behaviors;

import com.google.common.collect.ImmutableMap;
import com.themajorn.tuffgolem.common.entities.TuffGolemEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;

import java.util.Optional;
import java.util.function.Predicate;

public class TuffGolemLookTarget extends Behavior<TuffGolemEntity> {
    private final Predicate<LivingEntity> predicate;
    private final float maxDistSqr;
    private Optional<LivingEntity> nearestEntityMatchingTest = Optional.empty();

    public TuffGolemLookTarget(Predicate<LivingEntity> entities, float maxDist) {
        super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryStatus.VALUE_ABSENT, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryStatus.VALUE_PRESENT));
        this.predicate = entities;
        this.maxDistSqr = maxDist * maxDist;
    }

    protected boolean checkExtraStartConditions(ServerLevel serverLevel, TuffGolemEntity tuffGolem) {
        NearestVisibleLivingEntities nearestvisiblelivingentities = tuffGolem.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).get();
        this.nearestEntityMatchingTest = nearestvisiblelivingentities.findClosest(this.predicate.and((p_186053_) -> p_186053_.distanceToSqr(tuffGolem) <= (double)this.maxDistSqr));
        return this.nearestEntityMatchingTest.isPresent() && !tuffGolem.isPetrified();
    }

    protected void start(ServerLevel serverLevel, TuffGolemEntity tuffGolem, long l) {
        tuffGolem.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(this.nearestEntityMatchingTest.get(), true));
        this.nearestEntityMatchingTest = Optional.empty();
    }
}