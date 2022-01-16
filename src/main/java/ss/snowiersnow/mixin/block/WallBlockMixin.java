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
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import ss.snowiersnow.utils.SnowHelper;

@Mixin(WallBlock.class)
public class WallBlockMixin extends Block {

    public WallBlockMixin(Settings settings) {
        super(settings);
    }
    /**
     * @author snowier-snow si
     */
    @Overwrite
    private BlockState getStateWith(WorldView world, BlockState state, BlockPos pos, BlockState aboveState, boolean north, boolean east, boolean south, boolean west) {
        VoxelShape voxelShape = aboveState.getCollisionShape(world, pos).getFace(Direction.DOWN);
        pos = pos.down();
        north |= SnowHelper.getContentState(world, pos.north()).getBlock() instanceof WallBlock;
        east |= SnowHelper.getContentState(world, pos.east()).getBlock() instanceof WallBlock;
        south |= SnowHelper.getContentState(world, pos.south()).getBlock() instanceof WallBlock;
        west |= SnowHelper.getContentState(world, pos.west()).getBlock() instanceof WallBlock;
        BlockState blockState = this.getStateWith(state, north, east, south, west, voxelShape);
        return blockState.with(Properties.UP, this.shouldHavePost(blockState, aboveState, voxelShape));
    }

    @Shadow
    private boolean shouldHavePost(BlockState state, BlockState aboveState, VoxelShape aboveShape) { return true;}

    @Shadow
    private BlockState getStateWith(BlockState state, boolean north, boolean east, boolean south, boolean west, VoxelShape aboveShape) { return this.getDefaultState();}
}
