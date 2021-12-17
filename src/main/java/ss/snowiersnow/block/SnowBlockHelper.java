package ss.snowiersnow.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.SnowBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;
import ss.snowiersnow.SnowierSnow;

public class SnowBlockHelper {
    public static boolean canSetSnow(WorldView world, BlockPos pos, BlockState state) {
        if (doesNotSnow(world, pos)) {
            if (withinWorldHeight(world, pos)) {
                if (withinLightLimit(world, pos)) {
                    if (isSnowOrSnowloggable(state)) {
                        return canPlaceAt(world, pos);
                    }
                }
            }
        }
        return false;
    }


    private static boolean doesNotSnow(WorldView world, BlockPos pos) {
        return world.getBiome(pos).doesNotSnow(pos);
    }

    private static boolean withinWorldHeight(WorldView world, BlockPos pos) {
        return pos.getY() >= world.getBottomY() && pos.getY() < world.getTopY();
    }

    private static boolean withinLightLimit(WorldView world, BlockPos pos) {
        return world.getLightLevel(pos) < 10;
    }

    private static boolean isSnowOrSnowloggable(BlockState state) {
        return state.getBlock() instanceof SnowBlock || Snowloggable.contains(state);
    }

    private static boolean canPlaceAt(WorldView world, BlockPos pos) {
        return SnowierSnow.SNOW_BLOCK.getDefaultState().canPlaceAt(world, pos);
    }
}
