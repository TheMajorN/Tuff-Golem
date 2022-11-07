package com.themajorn.tuffgolem.common.ai.behaviors;

import com.google.common.collect.ImmutableMap;
import com.themajorn.tuffgolem.TuffGolem;
import com.themajorn.tuffgolem.common.entities.TuffGolemEntity;
import com.themajorn.tuffgolem.core.registry.ModMemoryModules;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class ForgetAboutItemFrame extends Behavior<TuffGolemEntity> {
    public static final int TIME_OUT_DURATION = 100;

    public ForgetAboutItemFrame() {
        super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED, MemoryModuleType.LONG_JUMP_MID_JUMP, MemoryStatus.VALUE_PRESENT), 100);

    }

    protected boolean checkExtraStartConditions(ServerLevel serverLevel, TuffGolemEntity tuffGolem) {
        return tuffGolem.getBrain().checkMemory(ModMemoryModules.SELECTED_ITEM_FRAME.get(), MemoryStatus.VALUE_PRESENT)
                && tuffGolem.getBrain().checkMemory(ModMemoryModules.SELECTED_ITEM_FRAME_POSITION.get(), MemoryStatus.VALUE_PRESENT)
                && !tuffGolem.hasItemInHand();
    }

    protected void start(ServerLevel serverLevel, TuffGolemEntity tuffGolem, long l) {
        tuffGolem.getBrain().eraseMemory(ModMemoryModules.SELECTED_ITEM_FRAME_POSITION.get());
        tuffGolem.getBrain().eraseMemory(ModMemoryModules.SELECTED_ITEM_FRAME.get());
        TuffGolem.LOGGER.info("Memories erased!");
    }
}