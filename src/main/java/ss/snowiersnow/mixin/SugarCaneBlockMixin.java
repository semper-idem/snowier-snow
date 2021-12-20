package ss.snowiersnow.mixin;


import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SugarCaneBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ss.snowiersnow.block.ISnowierBlock;
import ss.snowiersnow.block.SnowierBlockEntity;
import ss.snowiersnow.initializers.SnowierSnow;

import java.util.Optional;
import java.util.Random;

@Mixin(SugarCaneBlock.class)
public class SugarCaneBlockMixin extends Block {

    public SugarCaneBlockMixin(Settings settings) {
        super(settings);
    }

    /**
     * @author snnowier-snow akio
     */
    @Overwrite
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        System.out.println(state);
        if (!state.canPlaceAt(world, pos)) {
            world.breakBlock(pos, true);
        }

    }

    @Inject(at = @At(value = "HEAD"), method = "canPlaceAt", cancellable = true)
    private void onGetBlockStateInCanPlaceAt(BlockState state, WorldView world, BlockPos pos, CallbackInfoReturnable<Boolean> cir){
        if (world.getBlockState(pos.down()).getBlock() instanceof ISnowierBlock) {
            Optional<SnowierBlockEntity> snowierBlockEntity = world.getBlockEntity(pos.down(), SnowierSnow.SNOW_BE);
            if (snowierBlockEntity.isPresent()) {
                if (snowierBlockEntity.get().getContentState().isOf(Blocks.SUGAR_CANE)){
                    cir.setReturnValue(true);
                    cir.cancel();
                }
            }
        }
    }

}
