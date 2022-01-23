package ss.snowiersnow.mixin.block;


import net.minecraft.block.BlockState;
import net.minecraft.block.SnowyBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ss.snowiersnow.registry.ModTags;

@Mixin(SnowyBlock.class)
public class SnowyBlockMixin {

    @Inject( method = "isSnow", at = @At("RETURN"), cancellable = true)
    private static void isSnow(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(cir.getReturnValue() || state.isIn(ModTags.SNOW_BLOCK_TAG));
    }
}
