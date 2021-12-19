package ss.snowiersnow.block.helper;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import ss.snowiersnow.block.SnowierBlockEntity;

import java.util.Arrays;

public enum Snowloggable {
    DANDELION(Blocks.DANDELION),
    OAK_FENCE(Blocks.OAK_FENCE),
    OAK_FENCE_GATE(Blocks.OAK_FENCE_GATE),
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

    public static void setContent(WorldView world, BlockPos pos, BlockState content){
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof SnowierBlockEntity) {
            ((SnowierBlockEntity) blockEntity).setContent(content);
        }
    }

    public static BlockState getContent(BlockView world, BlockPos pos){
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof SnowierBlockEntity) {
            return ((SnowierBlockEntity)blockEntity).getContent();
        }
        return Blocks.AIR.getDefaultState();
    }

    public static boolean isEmpty(BlockView world, BlockPos pos){
        BlockEntity blockEntity = world.getBlockEntity(pos);
        System.out.println(blockEntity);
        if (blockEntity instanceof SnowierBlockEntity) {
            return ((SnowierBlockEntity)blockEntity).isEmpty();
        }
        return true;
    }

    public static void clear(WorldView world, BlockPos pos) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof SnowierBlockEntity) {
            ((SnowierBlockEntity)blockEntity).clear();
        }
    }
}
