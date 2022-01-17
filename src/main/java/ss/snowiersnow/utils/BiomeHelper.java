package ss.snowiersnow.utils;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.world.WorldView;
import ss.snowiersnow.registry.ModTags;

public class BiomeHelper {
    public static boolean canSetSnow(BlockState state, WorldView world, BlockPos pos) {
        if (!doesNotSnow(world, pos)) {
            if (withinWorldHeight(world, pos)) {
                if (withinLightLimit(world, pos)) {
                    if (isSnowOrSnowloggable(state) || state.isAir()) {
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
        return world.getLightLevel(LightType.BLOCK, pos) < 10;
    }

    private static boolean isSnowOrSnowloggable(BlockState state) {
        return state.isIn(ModTags.SNOW_BLOCK_TAG) || state.isIn(ModTags.SNOWLOGGABLE_TAG);
    }

    private static boolean canPlaceAt(WorldView world, BlockPos pos) {
        return Blocks.SNOW.getDefaultState().canPlaceAt(world, pos);
    }
}
