package com.themajorn.tuffgolem.core.registry;

import com.themajorn.tuffgolem.TuffGolem;
import com.themajorn.tuffgolem.common.items.BlockItemBase;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, TuffGolem.MOD_ID);

    public static final RegistryObject<Item> TUFF_VISAGE_ITEM = ITEMS.register("tuff_visage",
            () -> new BlockItemBase(ModBlocks.TUFF_VISAGE.get()));

}