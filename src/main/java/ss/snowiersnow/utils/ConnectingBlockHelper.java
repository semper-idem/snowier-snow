package ss.snowiersnow.utils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.PaneBlock;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import ss.snowiersnow.blockentity.ContentBlockEntity;
import ss.snowiersnow.registry.ModBlocks;


public class ConnectingBlockHelper {


    public static boolean isFenceConnective(BlockView world, BlockPos pos, Direction direction) {
        BlockState connectsTo = world.getBlockState(pos);
        if (connectsTo.isOf(ModBlocks.SNOW_WITH_CONTENT)) {
            BlockState content = ContentBlockEntity.getContent(world, pos);
            if (content.isIn(BlockTags.FENCES)) {
                connectsTo = content;
            }
        }
        boolean canConnect = !Block.cannotConnect(connectsTo);
        boolean isSolid = connectsTo.isSideSolidFullSquare(world, pos ,direction);
        boolean isFence = connectsTo.isIn(BlockTags.FENCES);
        return canConnect && (isSolid || isFence);
    }

    public static boolean isPaneConnective(BlockView world, BlockPos pos, Direction direction) {
        BlockState connectsTo = world.getBlockState(pos);
        boolean canConnect = !Block.cannotConnect(connectsTo);
        return canConnect &&
            (
                connectsTo.isSideSolidFullSquare(world, pos ,direction)
                || isPaneOrWall(connectsTo)
                || isContentPaneOrWall(connectsTo, world, pos)
            );
    }

    private static boolean isContentPaneOrWall(BlockState state, BlockView world, BlockPos pos) {
        if (state.isOf(ModBlocks.SNOW_WITH_CONTENT)) {
            return isPaneOrWall(ContentBlockEntity.getContent(world, pos));
        }
        return false;
    }
    private static boolean isPaneOrWall(BlockState state) {
        return state.getBlock() instanceof PaneBlock || state.isIn(BlockTags.WALLS);
    }
}