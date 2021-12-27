package ss.snowiersnow.mixin;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ss.snowiersnow.block.ModBlocks;
import ss.snowiersnow.utils.SnowHelper;

@Mixin(AbstractBlock.class)
public class AbstractBlockMixin {

    @Inject(method = "onUse", at = @At("TAIL"), cancellable = true)
    public void hookOnUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {
        Block blockInHand = Block.getBlockFromItem(player.getStackInHand(hand).getItem());
        if (blockInHand == ModBlocks.SNOW_BLOCK) {
            if (SnowHelper.canContain(state)) {
                if (ModBlocks.SNOW_BLOCK.canPlaceAt(state, world, pos)) {
                    SnowHelper.setSnow(state, world, pos);
                    cir.setReturnValue(ActionResult.success(world.isClient));
                }
            }
        }
    }
}
