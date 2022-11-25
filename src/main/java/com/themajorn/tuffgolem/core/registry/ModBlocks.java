package com.themajorn.tuffgolem.core.registry;

import com.themajorn.tuffgolem.TuffGolem;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.animal.allay.AllayAi;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, TuffGolem.MOD_ID);

}
