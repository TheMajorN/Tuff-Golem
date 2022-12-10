package com.themajorn.tuffgolem.core.registry;

import com.themajorn.tuffgolem.TuffGolem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PoweredBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, TuffGolem.MOD_ID);

    public static final RegistryObject<Block> REDSTONE_CONDUCTOR = BLOCKS.register("redstone_conductor",
            () -> new PoweredBlock(BlockBehaviour.Properties.of(Material.AIR).noCollission().noLootTable().air().isRedstoneConductor(ModBlocks::never)));

    private static boolean never(BlockState p_50806_, BlockGetter p_50807_, BlockPos p_50808_) {
        return false;
    }

}
