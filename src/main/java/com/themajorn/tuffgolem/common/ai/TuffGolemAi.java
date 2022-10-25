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
import net.minecraft.world.entity.animal.Wolf;
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
        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.useDefaultActivity();
        return brain;
    }

    private static void initCoreActivity(Brain<TuffGolemEntity> brain) {
        brain.addActivity(Activity.CORE, 0, ImmutableList.of(
                new Swim(0.8F),
                new LookAtTargetSink(45, 90),
                new MoveToTargetSink()
                ));
    }

    private static void initIdleActivity(Brain<TuffGolemEntity> brain) {
        brain.addActivityWithConditions(Activity.IDLE,
                ImmutableList.of(Pair.of(0, new RunOne<>(ImmutableList.of(Pair.of(new RandomStroll(1.0F), 2),
                                Pair.of(new SetWalkTargetFromLookTarget(1.0F, 3), 2),
                                Pair.of(new DoNothing(30, 60), 1))))),

                ImmutableSet.of());
    }
}
