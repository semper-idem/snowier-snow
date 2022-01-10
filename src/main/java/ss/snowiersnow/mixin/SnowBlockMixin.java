package ss.snowiersnow.mixin;


import net.minecraft.block.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import ss.snowiersnow.block.ModBlocks;


@Mixin(SnowBlock.class)
public class SnowBlockMixin extends Block {

    @Final
    @Shadow public static IntProperty LAYERS;
    @Final
    @Shadow protected static VoxelShape[] LAYERS_TO_SHAPE;

    public SnowBlockMixin(Settings settings) {
        super(settings);
    }


    /**
     * @author snowier-snow si
     */
    @Nullable
    @Overwrite
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockState blockState = ctx.getWorld().getBlockState(ctx.getBlockPos());
        if (blockState.getBlock() instanceof SnowBlock) {
            int i = blockState.get(LAYERS);
            return ModBlocks.SNOW.getDefaultState().with(LAYERS, Math.min(8, i + 1));
        } else {
            return super.getPlacementState(ctx);
        }
    }

    /**
     * @author snowier-snow si
     */
    @Overwrite
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        BlockState blockState = world.getBlockState(pos.down());
        if (!blockState.isOf(Blocks.ICE) && !blockState.isOf(Blocks.PACKED_ICE) && !blockState.isOf(Blocks.BARRIER)) {
            if (!blockState.isOf(Blocks.HONEY_BLOCK) && !blockState.isOf(Blocks.SOUL_SAND)) {
                return Block.isFaceFullSquare(blockState.getCollisionShape(world, pos.down()), Direction.UP) || blockState.getBlock() instanceof SnowBlock && blockState.get(LAYERS) == 8;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    /**
     * @author snowier-snow si
     */
    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        world.setBlockState(pos, ModBlocks.SNOW.getStateWithProperties(state));
    }

    /**
     * @author snowier-snow si
     */
    @Nullable
    @Overwrite
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return LAYERS_TO_SHAPE[state.get(LAYERS) > 2 ? state.get(LAYERS) - 2 : 0];
    }
}
