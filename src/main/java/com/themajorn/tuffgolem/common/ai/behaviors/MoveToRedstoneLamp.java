package com.themajorn.tuffgolem.common.ai.behaviors;

import com.google.common.collect.ImmutableMap;
import com.themajorn.tuffgolem.TuffGolem;
import com.themajorn.tuffgolem.common.entities.TuffGolemEntity;
import com.themajorn.tuffgolem.core.registry.ModMemoryModules;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.RedstoneLampBlock;
import net.minecraft.world.level.block.RedstoneTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Redstone;

import java.util.function.Predicate;

public class MoveToRedstoneLamp<E extends LivingEntity> extends Behavior<TuffGolemEntity> {

    private final int maxDistToWalk;
    private final float speedModifier;
    private boolean reachedTarget;
    protected int tryTicks;
    private final Predicate<E> predicate;


    public MoveToRedstoneLamp(Predicate<E> predicate, float speedMod, boolean memStatus, int interestRadius) {
        super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED,
                MemoryModuleType.WALK_TARGET, memStatus ? MemoryStatus.REGISTERED : MemoryStatus.VALUE_ABSENT,
                ModMemoryModules.NEAREST_REDSTONE_LAMP_MEMORY.get(), MemoryStatus.VALUE_PRESENT));
        this.maxDistToWalk = interestRadius;
        this.speedModifier = speedMod;
        this.predicate = predicate;
    }

    protected boolean checkExtraStartConditions(ServerLevel serverLevel, TuffGolemEntity entity) {
        boolean validStartConditions = this.getClosestRedstoneLampPos(entity).closerToCenterThan(entity.position(), maxDistToWalk)
                //&& getRedstoneLamp(entity).is(Blocks.REDSTONE_LAMP)
                //&& getRedstoneLamp(entity).getValue(RedstoneTorchBlock.LIT)
                && !entity.isPetrified();
        if (!validStartConditions) {

        } else {
            TuffGolem.LOGGER.info("Redstone lamp conditions met!");
        }
        return validStartConditions;
    }

    protected void start(ServerLevel serverLevel, TuffGolemEntity entity, long l) {
        BehaviorUtils.setWalkAndLookTargetMemories(entity, this.getClosestRedstoneLampPos(entity), this.speedModifier, 1);
        TuffGolem.LOGGER.info("Moving to redstone lamp!");

    }

    protected boolean canStillUse(ServerLevel serverLevel, TuffGolemEntity entity, long l) {
        boolean validContinueConditions = this.getClosestRedstoneLampPos(entity).closerToCenterThan(entity.position(), maxDistToWalk) && !entity.isPetrified();
        if (!validContinueConditions) {

        }
        return validContinueConditions;
    }
/*
    @Override
    protected void tick(ServerLevel serverLevel, TuffGolemEntity mob, long l) {
        BlockPos blockpos = this.getClosestRedstoneLampPos(mob);
        if (!blockpos.closerToCenterThan(mob.position(), maxDistToWalk)) {
            this.reachedTarget = false;
            ++this.tryTicks;
            if (this.shouldRecalculatePath()) {
                mob.getNavigation().moveTo((double)((float)blockpos.getX()) + 0.5D, (double)blockpos.getY(), (double)((float)blockpos.getZ()) + 0.5D, this.speedModifier);
            }
        } else {
            this.reachedTarget = true;
            --this.tryTicks;
        }

    }

 */

    public boolean shouldRecalculatePath() {
        return this.tryTicks % 40 == 0;
    }

    private BlockState getRedstoneLamp(TuffGolemEntity entity) {
        LevelReader reader = entity.level;
        return reader.getBlockState(getClosestRedstoneLampPos(entity));
    }

    private BlockPos getClosestRedstoneLampPos(TuffGolemEntity entity) {
        return entity.getBrain().getMemory(ModMemoryModules.NEAREST_REDSTONE_LAMP_MEMORY.get()).get();
    }

}
