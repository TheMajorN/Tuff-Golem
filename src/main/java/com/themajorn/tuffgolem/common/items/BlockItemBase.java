package com.themajorn.tuffgolem.common.items;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.Block;

public class BlockItemBase extends BlockItem {

    public BlockItemBase(Block block) {
        super(block, new Properties().tab(CreativeModeTab.TAB_BUILDING_BLOCKS));
    }
}
