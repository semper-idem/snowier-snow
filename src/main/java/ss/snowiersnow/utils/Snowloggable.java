package ss.snowiersnow.utils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.PaneBlock;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;

public class Snowloggable {
    private static final ArrayList<Block> SNOWLOGGABLE = new ArrayList<>();

    public static boolean canSnowContain(BlockState state) {
        return SNOWLOGGABLE.contains(state.getBlock());
    }

    public static void registerSnowloggableBlocks() {
        SNOWLOGGABLE.addAll(BlockTags.FENCES.values());
        SNOWLOGGABLE.addAll(BlockTags.FENCE_GATES.values());
        SNOWLOGGABLE.addAll(BlockTags.SAPLINGS.values());
        SNOWLOGGABLE.addAll(BlockTags.WALLS.values());
        SNOWLOGGABLE.addAll(BlockTags.FLOWERS.values());
        SNOWLOGGABLE.addAll(BlockTags.REPLACEABLE_PLANTS.values());
        SNOWLOGGABLE.add(Blocks.SUGAR_CANE);
        SNOWLOGGABLE.add(Blocks.BAMBOO);
        SNOWLOGGABLE.add(Blocks.BAMBOO_SAPLING);
        SNOWLOGGABLE.add(Blocks.SWEET_BERRY_BUSH);

        Registry.BLOCK.forEach(block -> {
            if (block instanceof PaneBlock) {
                SNOWLOGGABLE.add(block);
            }
        });
    }
}
