package ss.snowiersnow.mixin;

import net.minecraft.block.*;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.WorldView;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ss.snowiersnow.block.ISnowVariant;
import ss.snowiersnow.block.ModBlocks;
import ss.snowiersnow.utils.SnowHelper;

@Mixin(TallPlantBlock.class)
public class TallPlantBlockMixin extends PlantBlock {

    @Shadow @Final public static EnumProperty<DoubleBlockHalf> HALF;

    protected TallPlantBlockMixin(Settings settings) {
        super(settings);
    }


    @Inject(method = "getStateForNeighborUpdate", at = @At("TAIL"), cancellable = true)
    public void getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos, CallbackInfoReturnable<BlockState> cir) {
        BlockState contentDown = SnowHelper.getContentState(world, pos.down());
        if (state.get(HALF) == DoubleBlockHalf.UPPER) {
            if ((contentDown.getBlock() instanceof TallPlantBlock)) {
                if (contentDown.get(HALF) == DoubleBlockHalf.LOWER) {
                    cir.setReturnValue(state);
                }
            }
        }
    }

    @Inject(method = "onBreakInCreative", at = @At("TAIL"))
    private static void onBreakInCreative(World world, BlockPos pos, BlockState state, PlayerEntity player, CallbackInfo ci) {
        if (state.get(TallPlantBlock.HALF) == DoubleBlockHalf.UPPER) {
            BlockPos blockPos = pos.down();
            BlockState blockState = world.getBlockState(blockPos);
            if (blockState.getBlock() instanceof ISnowVariant) {
                world.setBlockState(blockPos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL | Block.SKIP_DROPS);
                world.setBlockState(blockPos, ModBlocks.SNOW_BLOCK.getDefaultState(), Block.NOTIFY_ALL | Block.SKIP_DROPS);
                world.syncWorldEvent(player, WorldEvents.BLOCK_BROKEN, blockPos, Block.getRawIdFromState(blockState));
            }
        }
    }

    @Inject(method = "canPlaceAt", at = @At("TAIL"), cancellable = true)
    public void canPlaceAt(BlockState state, WorldView world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (state.get(TallPlantBlock.HALF) == DoubleBlockHalf.LOWER) {
            cir.setReturnValue(super.canPlaceAt(state, world, pos));
        } else {
            System.out.println("TOP PART");
            BlockState blockState = world.getBlockState(pos.down());
            if (blockState.getBlock() instanceof ISnowVariant) {
                BlockState content = SnowHelper.getContentState(world, pos);
                if (content.getBlock() instanceof TallPlantBlock) {
                    cir.setReturnValue(content.get(TallPlantBlock.HALF) == DoubleBlockHalf.LOWER);
                }
            }
        }
    }
}
