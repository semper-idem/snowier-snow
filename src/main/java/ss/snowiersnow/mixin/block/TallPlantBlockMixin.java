package ss.snowiersnow.mixin.block;

import net.minecraft.block.*;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
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
import ss.snowiersnow.blockentity.ContentBlockEntity;
import ss.snowiersnow.registry.ModBlocks;

@Mixin(TallPlantBlock.class)
public class TallPlantBlockMixin extends PlantBlock {

    @Shadow @Final public static EnumProperty<DoubleBlockHalf> HALF;

    protected TallPlantBlockMixin(Settings settings) {
        super(settings);
    }


    @Inject(method = "getStateForNeighborUpdate", at = @At("HEAD"), cancellable = true)
    public void getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos, CallbackInfoReturnable<BlockState> cir) {
        BlockState contentDown = ContentBlockEntity.getContent(world, pos.down());
        if (state.get(HALF) == DoubleBlockHalf.UPPER) {
            if ((contentDown.getBlock() instanceof TallPlantBlock)) {
                if (contentDown.get(HALF) == DoubleBlockHalf.LOWER) {
                    cir.setReturnValue(state);
                }
            }
        }
    }

    @Inject(method = "onBreak", at = @At("HEAD"))
    private void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player, CallbackInfo ci) {
        if (state.get(TallPlantBlock.HALF) == DoubleBlockHalf.UPPER) {
            BlockPos posDown = pos.down();
            BlockState stateDown = world.getBlockState(posDown);
            if (stateDown.isOf(ModBlocks.SNOW_WITH_CONTENT)) {
                if (!player.isCreative()) {
                    dropStack(world, pos, new ItemStack(this.getDefaultState().getBlock()));
                }
                world.setBlockState(pos, Blocks.SNOW.getDefaultState().with(SnowBlock.LAYERS, stateDown.get(SnowBlock.LAYERS)));
                world.syncWorldEvent(player, WorldEvents.BLOCK_BROKEN, posDown, Block.getRawIdFromState(state));
            }
        }
        super.onBreak(world, pos, state, player);
    }

    @Inject(method = "canPlaceAt", at = @At("TAIL"), cancellable = true)
    public void canPlaceAt(BlockState state, WorldView world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (state.get(TallPlantBlock.HALF) == DoubleBlockHalf.LOWER) {
            cir.setReturnValue(super.canPlaceAt(state, world, pos));
        } else {
            BlockState stateDown = world.getBlockState(pos.down());
            if (stateDown.isOf(ModBlocks.SNOW_WITH_CONTENT)) {
                BlockState content = ContentBlockEntity.getContent(world, pos);
                if (content.getBlock() instanceof TallPlantBlock) {
                    cir.setReturnValue(content.get(TallPlantBlock.HALF) == DoubleBlockHalf.LOWER);
                }
            }
        }
    }
}
