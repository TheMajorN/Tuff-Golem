package com.themajorn.tuffgolem.common.blocks;

import com.themajorn.tuffgolem.common.entities.TuffGolemEntity;
import com.themajorn.tuffgolem.core.registry.ModBlocks;
import com.themajorn.tuffgolem.core.registry.ModEntities;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.level.block.state.pattern.BlockPatternBuilder;
import net.minecraft.world.level.block.state.predicate.BlockStatePredicate;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class TuffVisage extends HorizontalDirectionalBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    @Nullable
    private BlockPattern tuffGolemBase;
    @Nullable
    private BlockPattern tuffGolemFull;

    public TuffVisage(Properties properties) {
        super(properties);
    }

    @Override
    public void onPlace(BlockState p_60566_, @NotNull Level level, @NotNull BlockPos pos, BlockState p_60569_, boolean p_60570_) {
        this.trySpawnGolem(level, pos);
    }

    public boolean canSpawnGolem(LevelReader levelReader, BlockPos pos) {
        return this.getOrCreateTuffGolemBase().find(levelReader, pos) != null;
    }

    private BlockPattern getOrCreateTuffGolemBase() {
        if (this.tuffGolemBase == null) {
            this.tuffGolemBase = BlockPatternBuilder.start().aisle(" ", "#").where('#', BlockInWorld.hasState(BlockStatePredicate.forBlock(Blocks.TUFF))).build();
        }

        return this.tuffGolemBase;
    }

    private BlockPattern getOrCreateTuffGolemFull() {
        if (this.tuffGolemFull == null) {
            this.tuffGolemFull = BlockPatternBuilder.start().aisle("^", "#").where('^', BlockInWorld.hasState(BlockStatePredicate.forBlock(ModBlocks.TUFF_VISAGE.get()))).where('#', BlockInWorld.hasState(BlockStatePredicate.forBlock(Blocks.TUFF))).build();
        }
        return this.tuffGolemFull;
    }

    private void trySpawnGolem(Level level, BlockPos pos) {
        BlockPattern.BlockPatternMatch matchedPattern = this.getOrCreateTuffGolemFull().find(level, pos);
        if (matchedPattern != null) {
            for(int i = 0; i < this.getOrCreateTuffGolemFull().getHeight(); i++) {
                BlockInWorld blockinworld = matchedPattern.getBlock(0, i, 0);
                level.setBlock(blockinworld.getPos(), Blocks.AIR.defaultBlockState(), 2);
                level.levelEvent(2001, blockinworld.getPos(), Block.getId(blockinworld.getState()));
            }

            TuffGolemEntity tuffGolem = ModEntities.TUFF_GOLEM.get().create(level);
            BlockPos blockpos1 = matchedPattern.getBlock(0, 2, 0).getPos();

            assert tuffGolem != null;
            tuffGolem.moveTo((double)blockpos1.getX() + 0.5D, (double)blockpos1.getY() + 2.05D, (double)blockpos1.getZ() + 0.5D, 0.0F, 0.0F);
            level.addFreshEntity(tuffGolem);

            for(ServerPlayer serverplayer : level.getEntitiesOfClass(ServerPlayer.class, tuffGolem.getBoundingBox().inflate(5.0D))) {
                CriteriaTriggers.SUMMONED_ENTITY.trigger(serverplayer, tuffGolem);
            }

            for(int l = 0; l < this.getOrCreateTuffGolemFull().getHeight(); ++l) {
                BlockInWorld blockinworld3 = matchedPattern.getBlock(0, l, 0);
                level.blockUpdated(blockinworld3.getPos(), Blocks.AIR);
            }
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext p_51377_) {
        return this.defaultBlockState().setValue(FACING, p_51377_.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> p_51385_) {
        p_51385_.add(FACING);
    }
}
