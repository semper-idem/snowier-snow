package ss.snowiersnow.biome;

import net.minecraft.block.BlockState;
import net.minecraft.block.SnowBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.world.WorldView;
import ss.snowiersnow.block.ISnowierBlock;
import ss.snowiersnow.initializers.SnowierSnow;
import ss.snowiersnow.block.helper.Snowloggable;

public class BiomeHelper {
    public static boolean canSetSnow(WorldView world, BlockPos pos, BlockState state) {
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
        return state.getBlock() instanceof ISnowierBlock || Snowloggable.canContain(state);
    }

    private static boolean canPlaceAt(WorldView world, BlockPos pos) {
        return SnowierSnow.SNOW_BLOCK.getDefaultState().canPlaceAt(world, pos);
    }
}
