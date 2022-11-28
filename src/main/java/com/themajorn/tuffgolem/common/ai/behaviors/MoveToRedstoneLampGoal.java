package com.themajorn.tuffgolem.common.ai.behaviors;

import com.themajorn.tuffgolem.common.entities.TuffGolemEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FurnaceBlock;
import net.minecraft.world.level.block.RedstoneLampBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;

public class MoveToRedstoneLampGoal extends MoveToBlockGoal {
    private final TuffGolemEntity tuffGolem;

    public MoveToRedstoneLampGoal(TuffGolemEntity p_25149_, double p_25150_, int i) {
        super(p_25149_, p_25150_, 20);
        this.tuffGolem = p_25149_;
    }

    public boolean canUse() {
        return super.canUse() && !tuffGolem.isPetrified();
    }

    protected boolean isValidTarget(LevelReader p_25153_, BlockPos p_25154_) {
        if (!p_25153_.isEmptyBlock(p_25154_.above())) {
            return false;
        } else {
            BlockState blockstate = p_25153_.getBlockState(p_25154_);
            return blockstate.is(Blocks.REDSTONE_LAMP) && blockstate.getValue(RedstoneLampBlock.LIT);
        }
    }
}