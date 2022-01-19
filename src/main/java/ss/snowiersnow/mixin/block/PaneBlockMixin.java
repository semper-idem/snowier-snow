package ss.snowiersnow.mixin.block;


import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalConnectingBlock;
import net.minecraft.block.PaneBlock;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import ss.snowiersnow.utils.ConnectingBlockHelper;

@Mixin(PaneBlock.class)
public class PaneBlockMixin extends HorizontalConnectingBlock {
    protected PaneBlockMixin(float radius1, float radius2, float boundingHeight1, float boundingHeight2, float collisionHeight, Settings settings) {
        super(radius1, radius2, boundingHeight1, boundingHeight2, collisionHeight, settings);
    }

    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockView blockView = ctx.getWorld();
        BlockPos blockPos = ctx.getBlockPos();
        FluidState fluidState = ctx.getWorld().getFluidState(ctx.getBlockPos());
        return this.getDefaultState()
            .with(NORTH, ConnectingBlockHelper.isPaneConnective(blockView, blockPos.north(), Direction.SOUTH))
            .with(SOUTH, ConnectingBlockHelper.isPaneConnective(blockView, blockPos.south(), Direction.NORTH))
            .with(EAST, ConnectingBlockHelper.isPaneConnective(blockView, blockPos.east(), Direction.WEST))
            .with(WEST, ConnectingBlockHelper.isPaneConnective(blockView, blockPos.west(), Direction.EAST))
            .with(WATERLOGGED, fluidState.getFluid() == Fluids.WATER);
    }

    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (state.get(WATERLOGGED)) {
            world.createAndScheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }
        return direction.getAxis().isHorizontal() ?
            state.with(FACING_PROPERTIES.get(direction), ConnectingBlockHelper.isPaneConnective(world, neighborPos, direction.getOpposite())) :
            super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

}
