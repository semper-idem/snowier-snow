package ss.snowiersnow.mixin.block;


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
import ss.snowiersnow.blockentity.ContentBlockEntity;
import ss.snowiersnow.registry.ModBlocks;

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
        if (!state.canPlaceAt(world, pos)) {
            world.breakBlock(pos, true);
        }

    }

    @Inject(at = @At(value = "HEAD"), method = "canPlaceAt", cancellable = true)
    private void onGetBlockStateInCanPlaceAt(BlockState state, WorldView world, BlockPos pos, CallbackInfoReturnable<Boolean> cir){
        if (world.getBlockState(pos.down()).isOf(ModBlocks.SNOW_WITH_CONTENT)) {
            Optional<ContentBlockEntity> snowierBlockEntity = world.getBlockEntity(pos.down(), ModBlocks.CONTENT_ENTITY);
            if (snowierBlockEntity.isPresent()) {
                if (snowierBlockEntity.get().getContent().isOf(Blocks.SUGAR_CANE)){
                    cir.setReturnValue(true);
                    cir.cancel();
                }
            }
        }
    }
}
