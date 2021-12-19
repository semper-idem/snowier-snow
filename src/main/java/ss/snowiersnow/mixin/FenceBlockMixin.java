package ss.snowiersnow.mixin;


import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import ss.snowiersnow.block.SnowierBlockEntity;

@Mixin(FenceBlock.class)
public class FenceBlockMixin extends HorizontalConnectingBlock {


    protected FenceBlockMixin(float radius1, float radius2, float boundingHeight1, float boundingHeight2, float collisionHeight, Settings settings) {
        super(radius1, radius2, boundingHeight1, boundingHeight2, collisionHeight, settings);
    }

    /**
     * @author snowier-snow akio
     */
    @Overwrite
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockView blockView = ctx.getWorld();
        BlockPos blockPos = ctx.getBlockPos();
        FluidState fluidState = ctx.getWorld().getFluidState(ctx.getBlockPos());
        BlockPos blockPos2 = blockPos.north();
        BlockPos blockPos3 = blockPos.east();
        BlockPos blockPos4 = blockPos.south();
        BlockPos blockPos5 = blockPos.west();
        BlockState blockState = blockView.getBlockState(blockPos2);
        BlockState blockState2 = blockView.getBlockState(blockPos3);
        BlockState blockState3 = blockView.getBlockState(blockPos4);
        BlockState blockState4 = blockView.getBlockState(blockPos5);
        return super.getPlacementState(ctx)
            .with(NORTH, this.canConnect(blockState, blockState.isSideSolidFullSquare(blockView, blockPos2, Direction.SOUTH), Direction.SOUTH, blockView, blockPos2))
            .with(EAST, this.canConnect(blockState2, blockState2.isSideSolidFullSquare(blockView, blockPos3, Direction.WEST), Direction.WEST, blockView, blockPos3))
            .with(SOUTH, this.canConnect(blockState3, blockState3.isSideSolidFullSquare(blockView, blockPos4, Direction.NORTH), Direction.NORTH, blockView, blockPos4))
            .with(WEST, this.canConnect(blockState4, blockState4.isSideSolidFullSquare(blockView, blockPos5, Direction.EAST), Direction.EAST, blockView, blockPos5))
            .with(WATERLOGGED, fluidState.getFluid() == Fluids.WATER);
    }


    /**
     * @author snowier-snow akio
     */
    @Overwrite
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (state.get(WATERLOGGED)) {
            world.createAndScheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }

        return direction.getAxis().getType() == Direction.Type.HORIZONTAL ?
            state.with(FACING_PROPERTIES.get(direction), this.canConnect(neighborState, neighborState.isSideSolidFullSquare(world, neighborPos, direction.getOpposite()), direction.getOpposite(), world, pos)) :
            super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    public boolean canConnect(BlockState state, boolean neighborIsFullSquare, Direction dir, BlockView blockView, BlockPos blockPos) {
        Block block = state.getBlock();
        boolean bl = this.canConnectToFence(state);
        boolean bl2 = block instanceof FenceGateBlock && FenceGateBlock.canWallConnect(state, dir);
        boolean bl3 = state.hasBlockEntity() && canConnectToSnowlogged(state, blockView, blockPos);
        return !cannotConnect(state) && neighborIsFullSquare || bl || bl2 || bl3;
    }

    private boolean canConnectToSnowlogged(BlockState state, BlockView blockView, BlockPos pos) {
        BlockEntity blockEntity = blockView.getBlockEntity(pos);
        if (blockEntity instanceof SnowierBlockEntity) {
            BlockState content = ((SnowierBlockEntity) blockEntity).getContent();
            if (!content.isAir()) {
                return canConnectToFence(state);
            }
        }
        return true;
    }

    private boolean canConnectToFence(BlockState state) {
        return state.isIn(BlockTags.FENCES) && state.isIn(BlockTags.WOODEN_FENCES) == this.getDefaultState().isIn(BlockTags.WOODEN_FENCES);
    }
}
