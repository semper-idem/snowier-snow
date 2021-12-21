package ss.snowiersnow.mixin;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ss.snowiersnow.block.ISnowierBlock;
import ss.snowiersnow.block.SnowierBlockEntity;
import ss.snowiersnow.block.helper.Snowloggable;
import ss.snowiersnow.initializers.SnowierSnow;

import java.util.Optional;

@Mixin(AbstractBlock.class)
public class AbstractBlockMixin {

    @Inject(method = "onUse", at = @At("HEAD"), cancellable = true)
    private void onUseHook(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {
        state = world.getBlockState(pos); // for atm unknown reason after setting snowlayer old state is preserved
        if (Snowloggable.canContain(state)) {
            ActionResult result = addSnowLayer(state, world, pos, player, hand, hit);
            if (result != ActionResult.PASS) {
                cir.setReturnValue(result);
                cir.cancel();
            }
        }
    }

    private ActionResult addSnowLayer(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ItemStack stackInHand = player.getStackInHand(hand);
        if (stackInHand.isOf(SnowierSnow.SNOW_BLOCK.asItem())){
            if (state.getBlock() instanceof ISnowierBlock) {
                return SnowierSnow.SNOW_BLOCK.onUse(state, world, pos, player, hand, hit);
            }
            if (SnowierSnow.SNOW_BLOCK.getDefaultState().canPlaceAt(world, pos)) {
                world.setBlockState(pos, SnowierSnow.SNOW_BLOCK.getDefaultState(), Block.NOTIFY_LISTENERS);
                Optional<SnowierBlockEntity> snowierBlockEntity = world.getBlockEntity(pos, SnowierSnow.SNOW_BE);
                if (snowierBlockEntity.isPresent()) {
                    if (snowierBlockEntity.get().getContentState().isAir()) {
                        snowierBlockEntity.get().setContentState(state);
                    }
                }
                if (!player.isCreative()) {
                    stackInHand.decrement(1);
                }
                world.emitGameEvent(player, GameEvent.BLOCK_CHANGE, pos);
                return ActionResult.success(world.isClient);
            }
        }
        return ActionResult.PASS;
    }












    @Shadow
    public Item asItem() {
        return null;
    }

    @Shadow
    protected Block asBlock() {
        return null;
    }
}
