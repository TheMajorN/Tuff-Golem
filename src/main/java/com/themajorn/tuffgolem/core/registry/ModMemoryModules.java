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

    private static <U> MemoryModuleType<U> register(String p_26391_, Codec<U> p_26392_) {
        return Registry.register(Registry.MEMORY_MODULE_TYPE, new ResourceLocation(p_26391_), new MemoryModuleType<>(Optional.of(p_26392_)));
    }

    private static <U> MemoryModuleType<U> register(String p_26389_) {
        return Registry.register(Registry.MEMORY_MODULE_TYPE, new ResourceLocation(p_26389_), new MemoryModuleType<>(Optional.empty()));
    }

}
