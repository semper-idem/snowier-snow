package ss.snowiersnow.block.helper;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;

import java.util.ArrayList;

public class Snowloggable {
    private static final ArrayList<Block> snowloggableBlock = new ArrayList<>();
    /*
     check all rc hitbox
     bambo sugar sunflower check top level
     growing blocks bumbo sugar melon stem
     anvil/grindstone usage
     turtleegg onstep
     berrybush dmg
     oak sign message
    *
    *
    * */
    static {
        snowloggableBlock.add(Blocks.POPPY);
        snowloggableBlock.add(Blocks.BAMBOO);
        snowloggableBlock.add(Blocks.SUGAR_CANE);
        snowloggableBlock.add(Blocks.SUNFLOWER);
        snowloggableBlock.add(Blocks.MELON_STEM);
        snowloggableBlock.add(Blocks.ANVIL);
        snowloggableBlock.add(Blocks.GRINDSTONE);
        snowloggableBlock.add(Blocks.TURTLE_EGG);
        snowloggableBlock.add(Blocks.PLAYER_HEAD);
        snowloggableBlock.add(Blocks.SWEET_BERRY_BUSH);
        snowloggableBlock.add(Blocks.OAK_FENCE_GATE);
        snowloggableBlock.add(Blocks.OAK_FENCE);
        snowloggableBlock.add(Blocks.OAK_SIGN);
    }

    public static boolean canContain(BlockState state) {
        return canContain(state.getBlock());
    }

    public static boolean canContain(Block block) {
        return snowloggableBlock.contains(block);
    }
}
