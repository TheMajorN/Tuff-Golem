package com.themajorn.tuffgolem.common.ai;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import com.themajorn.tuffgolem.common.entities.TuffGolemEntity;
import com.themajorn.tuffgolem.core.registry.ModActivities;
import com.themajorn.tuffgolem.core.registry.ModMemoryModules;
import net.minecraft.client.renderer.entity.layers.WolfCollarLayer;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.allay.AllayAi;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.animal.goat.GoatAi;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PumpkinBlock;

import java.util.Optional;
import java.util.UUID;

public class TuffGolemAi {

    private static final UniformInt TIME_BETWEEN_ANIMATION_FROM_STATUE = UniformInt.of(600, 1200);

    public static Brain<?> makeBrain(Brain<TuffGolemEntity> brain) {
        initCoreActivity(brain);
        initIdleActivity(brain);
        initAnimateActivity(brain);
        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.useDefaultActivity();
        return brain;
    }

    private static void initCoreActivity(Brain<TuffGolemEntity> brain) {
        brain.addActivity(Activity.CORE, 0, ImmutableList.of(
                new Swim(0.8F),
                new LookAtTargetSink(45, 90),
                new MoveToTargetSink(),
                new CountDownCooldownTicks(ModMemoryModules.SPAWN_POSITION_COOLDOWN_TICKS)));
    }

    private static void initIdleActivity(Brain<TuffGolemEntity> brain) {
        brain.addActivityWithConditions(Activity.IDLE,
                ImmutableList.of(Pair.of(0, new GoToWantedItem<>((entity) -> {return true;}, 1.75F, true, 32)),
                                Pair.of(1, new ReturnToSpawnPosition<>(TuffGolemAi::getSpawnPosition, 1.25F)),
                                Pair.of(2, new StayCloseToSpawnPosition<>(TuffGolemAi::getSpawnPosition, 4, 16, 2.25F)),
                                Pair.of(4, new RunOne<>(ImmutableList.of(Pair.of(new RandomStroll(1.0F), 2),
                                Pair.of(new SetWalkTargetFromLookTarget(1.0F, 3), 2),
                                Pair.of(new DoNothing(30, 60), 1))))), ImmutableSet.of());
    }

    private static Optional<PositionTracker> getSpawnPosition(LivingEntity entity) {
        Brain<?> brain = entity.getBrain();
        Optional<GlobalPos> optional = brain.getMemory(ModMemoryModules.SPAWN_POSITION);
        if (optional.isPresent()) {
            GlobalPos globalpos = optional.get();
            if (shouldReturnToSpawn(entity, brain, globalpos)) {
                return Optional.of(new BlockPosTracker(globalpos.pos().above()));
            }

            brain.eraseMemory(ModMemoryModules.SPAWN_POSITION);
        }
        return getLikedPlayerPositionTracker(entity);
    }

    private static Optional<PositionTracker> getLikedPlayerPositionTracker(LivingEntity entity) {
        return getLikedPlayer(entity).map((player) -> {
            return new EntityTracker(player, true);
        });
    }

    public static Optional<ServerPlayer> getLikedPlayer(LivingEntity livingEntity) {
        Level level = livingEntity.getLevel();
        if (!level.isClientSide() && level instanceof ServerLevel serverlevel) {
            Optional<UUID> optional = livingEntity.getBrain().getMemory(MemoryModuleType.LIKED_PLAYER);
            if (optional.isPresent()) {
                Entity entity = serverlevel.getEntity(optional.get());
                if (entity instanceof ServerPlayer serverplayer) {
                    if ((serverplayer.gameMode.isSurvival() || serverplayer.gameMode.isCreative()) && serverplayer.closerThan(livingEntity, 64.0D)) {
                        return Optional.of(serverplayer);
                    }
                }

                return Optional.empty();
            }
        }

        return Optional.empty();
    }

    private static boolean shouldReturnToSpawn(LivingEntity entity, Brain<?> brain, GlobalPos globalPos) {
        Optional<Integer> optional = brain.getMemory(ModMemoryModules.SPAWN_POSITION_COOLDOWN_TICKS);
        Level level = entity.getLevel();
        return level.dimension() == globalPos.dimension() && optional.isPresent();
    }


    private static void initAnimateActivity(Brain<TuffGolemEntity> brain) {
        brain.addActivityWithConditions(ModActivities.ANIMATE, ImmutableList.of(Pair.of(0, new AnimateMidAnimation(TIME_BETWEEN_ANIMATION_FROM_STATUE, SoundEvents.ANVIL_STEP)), Pair.of(1, new AnimateFromStatue<>(TIME_BETWEEN_ANIMATION_FROM_STATUE, 5, 5, 1.5F, (entity) -> {
            return SoundEvents.ANVIL_DESTROY;
        }))), ImmutableSet.of(Pair.of(MemoryModuleType.TEMPTING_PLAYER, MemoryStatus.VALUE_ABSENT), Pair.of(MemoryModuleType.BREED_TARGET, MemoryStatus.VALUE_ABSENT), Pair.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT), Pair.of(ModMemoryModules.ANIMATE_FROM_STATUE_TICKS, MemoryStatus.VALUE_ABSENT)));
    }

}
