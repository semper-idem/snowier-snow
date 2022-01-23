package ss.snowiersnow.mixin.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.WallBlock;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.WorldView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ss.snowiersnow.utils.ConnectingBlockHelper;

@Mixin(WallBlock.class)
public class WallBlockMixin extends Block {

    public WallBlockMixin(Settings settings) {
        super(settings);
    }

    @Inject( method = "getStateWith(Lnet/minecraft/world/WorldView;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;ZZZZ)Lnet/minecraft/block/BlockState;", at = @At("RETURN"), cancellable = true)
    private void getStateWith(WorldView world, BlockState state, BlockPos pos, BlockState aboveState, boolean north, boolean east, boolean south, boolean west, CallbackInfoReturnable<BlockState> cir) {
        VoxelShape voxelShape = aboveState.getCollisionShape(world, pos).getFace(Direction.DOWN);
        pos = pos.down();
        north |= ConnectingBlockHelper.isWallConnective(world, pos.north(), Direction.SOUTH);
        east |= ConnectingBlockHelper.isWallConnective(world, pos.east(), Direction.WEST);
        south |= ConnectingBlockHelper.isWallConnective(world, pos.south(), Direction.NORTH);
        west |= ConnectingBlockHelper.isWallConnective(world, pos.west(), Direction.EAST);
        BlockState blockState = this.getStateWith(state, north, east, south, west, voxelShape);
        cir.setReturnValue(blockState.with(Properties.UP, this.shouldHavePost(blockState, aboveState, voxelShape)));
    }
    @Shadow
    private boolean shouldHavePost(BlockState state, BlockState aboveState, VoxelShape aboveShape) { return true;}

    @Shadow
    private BlockState getStateWith(BlockState state, boolean north, boolean east, boolean south, boolean west, VoxelShape aboveShape) { return this.getDefaultState();}
}
