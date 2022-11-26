package com.themajorn.tuffgolem.common.events;

import com.themajorn.tuffgolem.TuffGolem;
import com.themajorn.tuffgolem.common.ai.goals.TuffGolemTemptGoal;
import com.themajorn.tuffgolem.common.entities.TuffGolemEntity;
import com.themajorn.tuffgolem.core.registry.ModEntities;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityEvent;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.level.block.state.pattern.BlockPatternBuilder;
import net.minecraft.world.level.block.state.predicate.BlockStatePredicate;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.function.Predicate;

@Mod.EventBusSubscriber(modid = TuffGolem.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class TuffGolemEvents {

    private static final Predicate<BlockState> PUMPKINS_PREDICATE = (state) -> state != null && (state.is(Blocks.CARVED_PUMPKIN) || state.is(Blocks.JACK_O_LANTERN));
    private static final Ingredient PIG_FOOD = Ingredient.of(Items.CARROT, Items.POTATO, Items.BEETROOT);
    private static final Ingredient RABBIT_FOOD = Ingredient.of(Items.CARROT, Items.GOLDEN_CARROT, Items.DANDELION);

    @SubscribeEvent
    public static void spawnTuffGolem(BlockEvent.EntityPlaceEvent event) {
        Player player = (Player) event.getEntity();
        Level level = (Level) event.getLevel();
        BlockPattern tuffGolemBuilder = BlockPatternBuilder.start().aisle("^", "#").where('^', BlockInWorld.hasState(PUMPKINS_PREDICATE)).where('#', BlockInWorld.hasState(BlockStatePredicate.forBlock(Blocks.TUFF))).build();

        if (player.getItemInHand(InteractionHand.MAIN_HAND).is(Items.CARVED_PUMPKIN) || player.getItemInHand(InteractionHand.MAIN_HAND).is(Items.JACK_O_LANTERN)) {
            BlockPattern.BlockPatternMatch blockPatternMatch = tuffGolemBuilder.find(level, event.getPos());
            if (blockPatternMatch != null) {
                for(int i = 0; i < tuffGolemBuilder.getHeight(); ++i) {
                    BlockInWorld blockinworld = blockPatternMatch.getBlock(0, i, 0);
                    level.setBlock(blockinworld.getPos(), Blocks.AIR.defaultBlockState(), 2);
                    level.levelEvent(2001, blockinworld.getPos(), Block.getId(blockinworld.getState()));
                }

                TuffGolemEntity tuffGolem = ModEntities.TUFF_GOLEM.get().create(level);
                BlockPos matchedPatternPos = blockPatternMatch.getBlock(0, 2, 0).getPos();
                tuffGolem.moveTo((double)matchedPatternPos.getX() + 0.5D, (double)matchedPatternPos.getY() + 2.0D, (double)matchedPatternPos.getZ() + 0.5D, 0.0F, 0.0F);
                tuffGolem.setYRot(player.getYRot() * -1);
                level.addFreshEntity(tuffGolem);

                for(ServerPlayer serverplayer : level.getEntitiesOfClass(ServerPlayer.class, tuffGolem.getBoundingBox().inflate(5.0D))) {
                    CriteriaTriggers.SUMMONED_ENTITY.trigger(serverplayer, tuffGolem);
                }

                for(int l = 0; l < tuffGolemBuilder.getHeight(); ++l) {
                    BlockInWorld blockPatternMatchBlock = blockPatternMatch.getBlock(0, l, 0);
                    level.blockUpdated(blockPatternMatchBlock.getPos(), Blocks.AIR);
                }
            }
        }
    }

    @SubscribeEvent
    public static void initializeTuffGolemData(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof TuffGolemEntity tuffGolem) {
            tuffGolem.setGiving(false);
            tuffGolem.setReceiving(false);
            tuffGolem.setCanPickUpLoot(false);
            tuffGolem.setAnimated(true);
            tuffGolem.setAnimating(false);
            tuffGolem.setPetrified(false);
            tuffGolem.setPetrifying(false);
            tuffGolem.lockState(false);
        }
    }

    @SubscribeEvent
    public static void makeAnimalsFollowTuffGolem(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof Cow
            || event.getEntity() instanceof Sheep
            || event.getEntity() instanceof Llama
            || event.getEntity() instanceof MushroomCow) {
            ((Animal) event.getEntity()).goalSelector.addGoal(3, new TuffGolemTemptGoal((PathfinderMob) event.getEntity(), 1.25D, Ingredient.of(Items.WHEAT), false));
        }
        if (event.getEntity() instanceof Chicken) {
            ((Animal) event.getEntity()).goalSelector.addGoal(3, new TuffGolemTemptGoal((PathfinderMob) event.getEntity(), 1.25D, Ingredient.of(Items.WHEAT_SEEDS), false));
        }
        if (event.getEntity() instanceof Pig) {
            ((Animal) event.getEntity()).goalSelector.addGoal(3, new TuffGolemTemptGoal((PathfinderMob) event.getEntity(), 1.25D, PIG_FOOD, false));
        }
        if (event.getEntity() instanceof Bee) {
            ((Animal) event.getEntity()).goalSelector.addGoal(3, new TuffGolemTemptGoal((PathfinderMob) event.getEntity(), 1.25D, Ingredient.of(ItemTags.FLOWERS), false));
        }
        if (event.getEntity() instanceof Rabbit) {
            ((Animal) event.getEntity()).goalSelector.addGoal(3, new TuffGolemTemptGoal((PathfinderMob) event.getEntity(), 1.25D, RABBIT_FOOD, false));
        }
    }

}
