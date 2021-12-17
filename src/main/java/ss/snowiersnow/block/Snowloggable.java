package ss.snowiersnow.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;

import java.util.Arrays;

public enum Snowloggable {
    DANDELION(Blocks.DANDELION),
    POPPY(Blocks.POPPY);

    Block block;
    Snowloggable(Block block) {
        this.block = block;
    }


    public static boolean contains(BlockState state) {
        return contains(state.getBlock());
    }

    public static boolean contains(Block block) {
        return Arrays.stream(values()).anyMatch(snowloggable -> snowloggable.block == block);
    }
}
