package ss.snowiersnow.mixin.block;

import net.minecraft.block.BambooSaplingBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ss.snowiersnow.registry.ModBlocks;

@Mixin(BambooSaplingBlock.class)
public class BambooSaplingBlockMixin {


    @Inject(method = "getStateForNeighborUpdate", at=@At("TAIL"), cancellable = true)
    public void getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos, CallbackInfoReturnable<BlockState> cir) {
        if (state.canPlaceAt(world, pos))  {
            if (direction == Direction.UP && world.getBlockState(pos).isOf(ModBlocks.SNOW_WITH_CONTENT)) {
                cir.setReturnValue(Blocks.BAMBOO.getDefaultState());
            }
        }
    }
}
