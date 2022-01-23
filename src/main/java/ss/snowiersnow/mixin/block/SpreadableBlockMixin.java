package ss.snowiersnow.mixin.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.SnowyBlock;
import net.minecraft.block.SpreadableBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ss.snowiersnow.registry.ModTags;

@Mixin(SpreadableBlock.class)
public class SpreadableBlockMixin extends SnowyBlock {

    protected SpreadableBlockMixin(Settings settings) {
        super(settings);
    }

    @Inject(method = "canSurvive", at=@At("HEAD"), cancellable = true)
    private static void onCanSurvive(BlockState state, WorldView world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        BlockPos posUp = pos.up();
        BlockState stateUp = world.getBlockState(posUp);
        if (stateUp.isIn(ModTags.SNOW_BLOCK_TAG)) {
            cir.setReturnValue(true);
        }
    }

    @Redirect(method = "randomTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)Z", ordinal = 1))
    public boolean randomTick(ServerWorld instance, BlockPos blockPos, BlockState blockState) {
        return instance.setBlockState(blockPos, blockState.with(SNOWY, instance.getBlockState(blockPos.up()).isIn(ModTags.SNOW_BLOCK_TAG)));
    }
}
