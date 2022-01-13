package ss.snowiersnow.mixin.block;


import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FenceBlock;
import net.minecraft.block.HorizontalConnectingBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ss.snowiersnow.block.ModBlocks;
import ss.snowiersnow.utils.SnowHelper;

@Mixin(FenceBlock.class)
public class FenceBlockMixin extends HorizontalConnectingBlock {


    protected FenceBlockMixin(float radius1, float radius2, float boundingHeight1, float boundingHeight2, float collisionHeight, Settings settings) {
        super(radius1, radius2, boundingHeight1, boundingHeight2, collisionHeight, settings);
    }

    @Shadow
    public boolean canConnect(BlockState state, boolean neighborIsFullSquare, Direction dir) {return false;}

    @Inject(method = "onUse", at = @At("HEAD"), cancellable = true)
    public void onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {
        if (Block.getBlockFromItem(player.getStackInHand(hand).getItem()) == ModBlocks.SNOW_WITH_CONTENT) {
            if (SnowHelper.canContain(state)) {
                if (ModBlocks.SNOW_WITH_CONTENT.canPlaceAt(state, world, pos)) {
                    SnowHelper.setSnow(state, world, pos);
                    if (!player.isCreative()) {
                        player.getStackInHand(hand).decrement(1);
                    }
                    cir.setReturnValue(ActionResult.success(world.isClient));
                }
            }
        }
    }

    /**
     * @author
     */
    @Overwrite
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        WorldAccess blockView = ctx.getWorld();
        BlockPos blockPos = ctx.getBlockPos();
        FluidState fluidState = ctx.getWorld().getFluidState(ctx.getBlockPos());
        BlockPos northPos = blockPos.north();
        BlockPos eastPos = blockPos.east();
        BlockPos southPos = blockPos.south();
        BlockPos westPos = blockPos.west();
        BlockState northState = blockView.getBlockState(northPos);
        BlockState eastState = blockView.getBlockState(eastPos);
        BlockState southState = blockView.getBlockState(southPos);
        BlockState westState = blockView.getBlockState(westPos);
        boolean northConnected = SnowHelper.isContentFence(blockView, northPos) || northState.isSideSolidFullSquare(blockView, northPos, Direction.SOUTH);
        boolean southConnected = SnowHelper.isContentFence(blockView, southPos) || northState.isSideSolidFullSquare(blockView, southPos, Direction.NORTH);
        boolean eastConnected = SnowHelper.isContentFence(blockView, eastPos) || northState.isSideSolidFullSquare(blockView, eastPos, Direction.WEST);
        boolean westConnected = SnowHelper.isContentFence(blockView, westPos) || northState.isSideSolidFullSquare(blockView, westPos, Direction.EAST);
        return super.getPlacementState(ctx)
            .with(NORTH, this.canConnect(northState, northConnected, Direction.SOUTH))
            .with(EAST, this.canConnect(eastState, eastConnected, Direction.WEST))
            .with(SOUTH, this.canConnect(southState, southConnected, Direction.NORTH))
            .with(WEST, this.canConnect(westState, westConnected, Direction.EAST))
            .with(WATERLOGGED, fluidState.getFluid() == Fluids.WATER);
    }

}
