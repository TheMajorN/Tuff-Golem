package com.themajorn.tuffgolem.core.registry;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.serialization.Codec;
import com.themajorn.tuffgolem.TuffGolem;
import com.themajorn.tuffgolem.common.entities.TuffGolemEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.ai.memory.ExpirableValue;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Optional;
import java.util.UUID;

public class ModMemoryModules<U> {

    public static final DeferredRegister<MemoryModuleType<?>> MEMORY_MODULES = DeferredRegister.create(ForgeRegistries.MEMORY_MODULE_TYPES, TuffGolem.MOD_ID);


    public static final RegistryObject<MemoryModuleType<GlobalPos>> ITEM_FRAME_POSITION = MEMORY_MODULES.register("item_frame_position",
            () -> new MemoryModuleType<>(Optional.empty()));

    public static final RegistryObject<MemoryModuleType<BlockPos>> SELECTED_ITEM_FRAME_POSITION = MEMORY_MODULES.register("selected_item_frame_position",
            () -> new MemoryModuleType<>(Optional.empty()));

    public static final RegistryObject<MemoryModuleType<Integer>> GO_TO_ITEM_FRAME_COOLDOWN_TICKS = MEMORY_MODULES.register("item_frame_cooldown_ticks",
            () -> new MemoryModuleType<>(Optional.of(Codec.INT)));

    public static final RegistryObject<MemoryModuleType<Integer>> GO_TO_CHEST_COOLDOWN_TICKS = MEMORY_MODULES.register("chest_cooldown_ticks",
            () -> new MemoryModuleType<>(Optional.of(Codec.INT)));

    public static final RegistryObject<MemoryModuleType<Integer>> ANIMATE_OR_PETRIFY_COOLDOWN_TICKS = MEMORY_MODULES.register("animate_or_petrify_cooldown_ticks",
            () -> new MemoryModuleType<>(Optional.of(Codec.INT)));

    public static final RegistryObject<MemoryModuleType<ItemFrame>> SELECTED_ITEM_FRAME = MEMORY_MODULES.register("selected_item_frame",
            () -> new MemoryModuleType<>(Optional.empty()));

    public static final RegistryObject<MemoryModuleType<Boolean>> MID_ANIMATE_OR_PETRIFY = MEMORY_MODULES.register("mid_animate_or_petrify",
            () -> new MemoryModuleType<>(Optional.empty()));

    public static final RegistryObject<MemoryModuleType<ItemFrame>> NEAREST_VISIBLE_ITEM_FRAME = MEMORY_MODULES.register("nearest_visible_item_frame",
            () -> new MemoryModuleType<>(Optional.empty()));

    public static final RegistryObject<MemoryModuleType<ChestBlockEntity>> NEAREST_COMPATIBLE_CHEST = MEMORY_MODULES.register("nearest_compatible_chest",
            () -> new MemoryModuleType<>(Optional.empty()));

    public static final RegistryObject<MemoryModuleType<TuffGolemEntity>> STACK_TARGET = MEMORY_MODULES.register("stack_target",
            () -> new MemoryModuleType<>(Optional.empty()));

    private final Optional<Codec<ExpirableValue<U>>> codec;

    @VisibleForTesting
    public ModMemoryModules(Optional<Codec<U>> codec) {
        this.codec = codec.map(ExpirableValue::codec);
    }

    public Optional<Codec<ExpirableValue<U>>> getCodec() {
        return this.codec;
    }
}
