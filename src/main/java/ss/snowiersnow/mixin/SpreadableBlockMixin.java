package ss.snowiersnow.mixin;

import net.minecraft.block.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldView;
import net.minecraft.world.chunk.light.ChunkLightProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import ss.snowiersnow.block.ModBlocks;

import java.util.Random;

@Mixin(SpreadableBlock.class)
public class SpreadableBlockMixin extends SnowyBlock {


    protected SpreadableBlockMixin(Settings settings) {
        super(settings);
    }

    /**
     * @author snowier-snow si
     */
    @Overwrite
    private static boolean canSurvive(BlockState state, WorldView world, BlockPos pos) {
        BlockPos posUp = pos.up();
        BlockState stateUp = world.getBlockState(posUp);
        if (stateUp.isOf(ModBlocks.SNOW)) {
            return true;
        } else if (stateUp.getFluidState().getLevel() == 8) {
            return false;
        } else {
            int i = ChunkLightProvider.getRealisticOpacity(world, state, pos, stateUp, posUp, Direction.UP, stateUp.getOpacity(world, posUp));
            return i < world.getMaxLightLevel();
        }
    }

    /**
     * @author snowier-snow si
     */
    @Overwrite
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (!canSurvive(state, world, pos)) {
            world.setBlockState(pos, Blocks.DIRT.getDefaultState());
        } else {
            if (world.getLightLevel(pos.up()) >= 9) {
                BlockState blockState = this.getDefaultState();

                for(int i = 0; i < 4; ++i) {
                    BlockPos blockPos = pos.add(random.nextInt(3) - 1, random.nextInt(5) - 3, random.nextInt(3) - 1);
                    if (world.getBlockState(blockPos).isOf(Blocks.DIRT) && canSpread(blockState, world, blockPos)) {
                        BlockState blockStateUp = world.getBlockState(blockPos.up());
                        boolean isUpSnow = (blockStateUp.isOf(Blocks.SNOW) || blockStateUp.isOf(ModBlocks.SNOW));
                        world.setBlockState(blockPos, (BlockState)blockState.with(SNOWY, isUpSnow));
                    }
                }
            }

        }
    }

    @Shadow
    private static boolean canSpread(BlockState state, WorldView world, BlockPos pos) {return false;}
}
