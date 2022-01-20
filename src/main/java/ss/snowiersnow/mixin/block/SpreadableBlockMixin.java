package ss.snowiersnow.mixin.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SnowyBlock;
import net.minecraft.block.SpreadableBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ss.snowiersnow.registry.ModTags;

import java.util.Random;

@Mixin(SpreadableBlock.class)
public class SpreadableBlockMixin extends SnowyBlock {

    protected SpreadableBlockMixin(Settings settings) {
        super(settings);
    }

    @Shadow
    private static boolean canSurvive(BlockState state, WorldView world, BlockPos pos) { return true;}

    @Inject(method = "canSurvive", at=@At("HEAD"), cancellable = true)
    private static void onCanSurvive(BlockState state, WorldView world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        BlockPos posUp = pos.up();
        BlockState stateUp = world.getBlockState(posUp);
        if (stateUp.isIn(ModTags.SNOW_BLOCK_TAG)) {
            cir.setReturnValue(true);
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
                        world.setBlockState(blockPos, blockState.with(SNOWY, world.getBlockState(blockPos.up()).isIn(ModTags.SNOW_BLOCK_TAG)));
                    }
                }
            }

        }
    }

    @Shadow
    private static boolean canSpread(BlockState state, WorldView world, BlockPos pos) {return false;}
}
