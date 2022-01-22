package ss.snowiersnow.mixin.block;


import net.minecraft.block.BlockState;
import net.minecraft.block.FenceBlock;
import net.minecraft.block.HorizontalConnectingBlock;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import ss.snowiersnow.utils.ConnectingBlockHelper;

@Mixin(FenceBlock.class)
public class FenceBlockMixin extends HorizontalConnectingBlock {
    protected FenceBlockMixin(float radius1, float radius2, float boundingHeight1, float boundingHeight2, float collisionHeight, Settings settings) {
        super(radius1, radius2, boundingHeight1, boundingHeight2, collisionHeight, settings);
    }

    /**
     * @author snowier-snow si
     */
    @Overwrite
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        WorldAccess world = ctx.getWorld();
        BlockPos blockPos = ctx.getBlockPos();
        FluidState fluidState = ctx.getWorld().getFluidState(ctx.getBlockPos());
        return super.getPlacementState(ctx)
            .with(NORTH, ConnectingBlockHelper.isFenceConnective(world, blockPos.north(), Direction.SOUTH))
            .with(EAST, ConnectingBlockHelper.isFenceConnective(world, blockPos.east(), Direction.WEST))
            .with(SOUTH, ConnectingBlockHelper.isFenceConnective(world, blockPos.south(), Direction.NORTH))
            .with(WEST, ConnectingBlockHelper.isFenceConnective(world, blockPos.west(), Direction.EAST))
            .with(WATERLOGGED, fluidState.getFluid() == Fluids.WATER);
    }


    /**
     * @author snowier-snow si
     */
    @Overwrite
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (state.get(WATERLOGGED)) {
            world.createAndScheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }
        return direction.getAxis().getType() == Direction.Type.HORIZONTAL ?
            state.with(FACING_PROPERTIES.get(direction), ConnectingBlockHelper.isFenceConnective(world, neighborPos, direction.getOpposite())) :
            super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }
}
