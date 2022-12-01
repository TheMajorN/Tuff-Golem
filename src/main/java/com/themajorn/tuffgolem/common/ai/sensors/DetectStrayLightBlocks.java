package com.themajorn.tuffgolem.common.ai.sensors;

import com.google.common.collect.ImmutableSet;
import com.themajorn.tuffgolem.TuffGolem;
import com.themajorn.tuffgolem.common.entities.TuffGolemEntity;
import com.themajorn.tuffgolem.core.registry.ModMemoryModules;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;

import java.util.Set;

public class DetectStrayLightBlocks extends Sensor<TuffGolemEntity> {
    private static final long XZ_RANGE = 6L;
    private static final long Y_RANGE = 6L;
    protected int verticalSearchStart = 0;
    protected BlockPos blockPos = BlockPos.ZERO;

    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(ModMemoryModules.NEAREST_REDSTONE_LAMP_MEMORY.get());
    }
    protected void doTick(ServerLevel level, TuffGolemEntity mob) {
        Brain<?> brain = mob.getBrain();
        int i = (int) XZ_RANGE;
        int j = (int) Y_RANGE;
        BlockPos mobPos = mob.getOnPos().above();
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

        for(int k = this.verticalSearchStart; k <= j; k = k > 0 ? -k : 1 - k) {
            for(int l = 0; l < i; ++l) {
                for(int i1 = 0; i1 <= l; i1 = i1 > 0 ? -i1 : 1 - i1) {
                    for(int j1 = i1 < l && i1 > -l ? l : 0; j1 <= l; j1 = j1 > 0 ? -j1 : 1 - j1) {
                        mutableBlockPos.setWithOffset(mobPos, i1, k - 1, j1);
                        if (mob.isWithinRestriction(mutableBlockPos) && isValidTarget(mob.level, mutableBlockPos) && mutableBlockPos != mobPos) {
                            //blockPos = mutableBlockPos;
                            mob.level.setBlockAndUpdate(mutableBlockPos, Blocks.AIR.defaultBlockState());
                            TuffGolem.LOGGER.info("Deleting stray light block at " + mutableBlockPos.getX() + " " + mutableBlockPos.getY() + " " + mutableBlockPos.getZ());
                        }
                    }
                }
            }
        }
    }

    protected boolean isValidTarget(LevelReader levelReader, BlockPos blockPos) {
        return levelReader.getBlockState(blockPos).is(Blocks.LIGHT);
    }
}