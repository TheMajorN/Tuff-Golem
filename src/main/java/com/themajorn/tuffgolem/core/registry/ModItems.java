package com.themajorn.tuffgolem.core.registry;

import com.themajorn.tuffgolem.TuffGolem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, TuffGolem.MOD_ID);

    public static final RegistryObject<BlockItem> REDSTONE_CONDUCTOR = ITEMS.register("redstone_conductor",
            () -> new BlockItem(ModBlocks.REDSTONE_CONDUCTOR.get(), new Item.Properties()));

}
