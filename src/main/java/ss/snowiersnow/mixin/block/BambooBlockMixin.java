package ss.snowiersnow.mixin.block;

import net.minecraft.block.BambooBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.enums.BambooLeaves;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ss.snowiersnow.utils.SnowHelper;

@Mixin(BambooBlock.class)
public class BambooBlockMixin extends Block{
    @Final
    @Shadow
    public static EnumProperty<BambooLeaves> LEAVES;
    @Final
    @Shadow
    public static IntProperty AGE;


    public BambooBlockMixin(Settings settings) {
        super(settings);
    }

    /**
     * @author snowier-snow akio
     */
    @Nullable
    @Overwrite
    public  BlockState getPlacementState(ItemPlacementContext ctx) {
        FluidState fluidState = ctx.getWorld().getFluidState(ctx.getBlockPos());
        if (!fluidState.isEmpty()) {
            return null;
        } else {
            BlockState blockState = ctx.getWorld().getBlockState(ctx.getBlockPos().down());
            BlockState content = SnowHelper.getContentState(ctx.getWorld(), ctx.getBlockPos().down());
            if (blockState.isIn(BlockTags.BAMBOO_PLANTABLE_ON) || content.isIn(BlockTags.BAMBOO_PLANTABLE_ON)) {
                if (blockState.isOf(Blocks.BAMBOO_SAPLING) || content.isIn(BlockTags.BAMBOO_PLANTABLE_ON)) {
                    return this.getDefaultState().with(AGE, 0);
                } else if (blockState.isOf(Blocks.BAMBOO) || content.isOf(Blocks.BAMBOO)) {
                    int i = blockState.get(AGE) > 0 ? 1 : 0;
                    return this.getDefaultState().with(AGE, i);
                } else {
                    BlockState i = ctx.getWorld().getBlockState(ctx.getBlockPos().up());
                    return (i.isOf(Blocks.BAMBOO) || content.isOf(Blocks.BAMBOO)) ? this.getDefaultState().with(AGE, i.get(AGE)) : Blocks.BAMBOO_SAPLING.getDefaultState();
                }
            } else {
                return null;
            }
        }
    }


    /**
     * @author snowier-snow akio
     */
    @Overwrite
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        return (world.getBlockState(pos.down()).isIn(BlockTags.BAMBOO_PLANTABLE_ON) || SnowHelper.getContentState(world, pos.down()).isIn(BlockTags.BAMBOO_PLANTABLE_ON));
    }


    @Inject(at = @At("HEAD"), method = "getStateForNeighborUpdate")
    public void onGetStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos, CallbackInfoReturnable<BlockState> cir) {
        BlockState neighborContent = SnowHelper.getContentState(world, neighborPos);
        if (neighborContent.isOf(Blocks.BAMBOO)) {
            if (direction == Direction.UP && neighborContent.get(AGE) > state.get(AGE)) {
                world.setBlockState(pos, state.cycle(AGE), Block.NOTIFY_LISTENERS);
            }
        }
    }


    /**
     * @author snowier-snow akio
     */
    @Overwrite
    public int countBambooAbove(BlockView world, BlockPos pos) {
        int i;
        for(i = 0; i < 16 && (world.getBlockState(pos.up(i + 1)).isOf(Blocks.BAMBOO) || SnowHelper.getContentState(world, pos).isOf(Blocks.BAMBOO)); ++i) {
        }

        return i;
    }

    /**
     * @author snowier-snow akio
     */
    @Overwrite
    public int countBambooBelow(BlockView world, BlockPos pos) {
        int i;
        for(i = 0; i < 16 && (world.getBlockState(pos.up(i + 1)).isOf(Blocks.BAMBOO) || SnowHelper.getContentState(world, pos).isOf(Blocks.BAMBOO)); ++i) {
        }

        return i;
    }
}
