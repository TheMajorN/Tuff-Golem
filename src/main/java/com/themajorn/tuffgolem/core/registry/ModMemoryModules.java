package com.themajorn.tuffgolem.core.registry;

import com.mojang.serialization.Codec;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Registry;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

import java.util.Optional;
import java.util.UUID;

public class ModMemoryModules<U> {

    public static final MemoryModuleType<Integer> ANIMATE_FROM_STATUE_TICKS = register("animate_from_statue_cooling_down", Codec.INT);
    public static final MemoryModuleType<Boolean> ANIMATE_MID_ANIMATE = register("animate_mid_animate");
    public static final MemoryModuleType<GlobalPos> SPAWN_POSITION = register("spawn_position", GlobalPos.CODEC);
    public static final MemoryModuleType<Integer> SPAWN_POSITION_COOLDOWN_TICKS = register("spawn_position_cooldown_ticks", Codec.INT);
    public static final MemoryModuleType<UUID> PLAYER_WHO_GAVE_ITEM = register("player_who_gave_item", UUIDUtil.CODEC);

    private static <U> MemoryModuleType<U> register(String p_26391_, Codec<U> p_26392_) {
        return Registry.register(Registry.MEMORY_MODULE_TYPE, new ResourceLocation(p_26391_), new MemoryModuleType<>(Optional.of(p_26392_)));
    }

    private static <U> MemoryModuleType<U> register(String p_26389_) {
        return Registry.register(Registry.MEMORY_MODULE_TYPE, new ResourceLocation(p_26389_), new MemoryModuleType<>(Optional.empty()));
    }

}
