package ss.snowiersnow.utils;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.world.WorldView;
import ss.snowiersnow.registry.ModTags;

public class BiomeHelper {
    public static boolean canAddSnowLayer(BlockState state, WorldView world, BlockPos pos) {
       return !doesNotSnow(world, pos) &&
           withinWorldHeight(world, pos) &&
           withinLightLimit(world, pos) &&
           isSnowloggable(state) &&
           canPlaceAt(world, pos);
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

    private static boolean isSnowloggable(BlockState state) {
        return state.isIn(ModTags.SNOW_BLOCK_TAG) || state.isAir() || Snowloggable.canSnowContain(state);
    }

    private static boolean canPlaceAt(WorldView world, BlockPos pos) {
        return Blocks.SNOW.getDefaultState().canPlaceAt(world, pos);
    }
}
