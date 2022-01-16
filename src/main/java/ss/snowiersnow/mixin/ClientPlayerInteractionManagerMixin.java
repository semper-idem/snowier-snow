package ss.snowiersnow.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ss.snowiersnow.block.ModBlocks;

@Mixin(ClientPlayerInteractionManager.class)
public class ClientPlayerInteractionManagerMixin {

    @Shadow
	private MinecraftClient client;

    @Inject(method = "breakBlock", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getFluidState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/fluid/FluidState;"))
    public void breakBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (client.world != null) {
            if (client.world.getBlockState(pos).isIn(ModBlocks.SNOW_TAG)) {
                cir.setReturnValue(true);
                cir.cancel();
            }
        }
    }

}
