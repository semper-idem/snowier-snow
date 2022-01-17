package ss.snowiersnow.mixin.block;


import net.minecraft.block.BlockState;
import net.minecraft.block.FenceBlock;
import net.minecraft.block.HorizontalConnectingBlock;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import ss.snowiersnow.blockentity.ContentBlockEntity;

@Mixin(FenceBlock.class)
public class FenceBlockMixin extends HorizontalConnectingBlock {
    protected FenceBlockMixin(float radius1, float radius2, float boundingHeight1, float boundingHeight2, float collisionHeight, Settings settings) {
        super(radius1, radius2, boundingHeight1, boundingHeight2, collisionHeight, settings);
    }

    @Shadow
    public boolean canConnect(BlockState state, boolean neighborIsFullSquare, Direction dir) {return false;}

    /**
     * @author snowier-snow si
     */
    @Overwrite
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        WorldAccess world = ctx.getWorld();
        BlockPos blockPos = ctx.getBlockPos();
        FluidState fluidState = ctx.getWorld().getFluidState(ctx.getBlockPos());
        BlockPos northPos = blockPos.north();
        BlockPos eastPos = blockPos.east();
        BlockPos southPos = blockPos.south();
        BlockPos westPos = blockPos.west();
        BlockState northState = world.getBlockState(northPos);
        BlockState eastState = world.getBlockState(eastPos);
        BlockState southState = world.getBlockState(southPos);
        BlockState westState = world.getBlockState(westPos);
        boolean northConnected = isNeighbourFence(world, northPos) || northState.isSideSolidFullSquare(world, northPos, Direction.SOUTH);
        boolean southConnected = isNeighbourFence(world, southPos) || northState.isSideSolidFullSquare(world, southPos, Direction.NORTH);
        boolean eastConnected = isNeighbourFence(world, eastPos) || northState.isSideSolidFullSquare(world, eastPos, Direction.WEST);
        boolean westConnected = isNeighbourFence(world, westPos) || northState.isSideSolidFullSquare(world, westPos, Direction.EAST);
        return super.getPlacementState(ctx)
            .with(NORTH, this.canConnect(northState, northConnected, Direction.SOUTH))
            .with(EAST, this.canConnect(eastState, eastConnected, Direction.WEST))
            .with(SOUTH, this.canConnect(southState, southConnected, Direction.NORTH))
            .with(WEST, this.canConnect(westState, westConnected, Direction.EAST))
            .with(WATERLOGGED, fluidState.getFluid() == Fluids.WATER);
    }

    private boolean isNeighbourFence(WorldAccess world, BlockPos pos) {
        return ContentBlockEntity.getContent(world, pos).isIn(BlockTags.FENCES); //TODO Fences vs Wooden Fences?
    }
}
