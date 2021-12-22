package ss.snowiersnow.mixin;


import net.minecraft.block.*;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import ss.snowiersnow.SnowierSnow;
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
     * @author snowier-snow akio
     */
    @Nullable
    @Overwrite
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockState blockState = ctx.getWorld().getBlockState(ctx.getBlockPos());
        if (blockState.getBlock() instanceof SnowBlock) {
            int i = blockState.get(LAYERS);
            return ModBlocks.SNOW_BLOCK.getDefaultState().with(LAYERS, Math.min(8, i + 1));
        } else {
            return super.getPlacementState(ctx);
        }
    }


    /**
     * @author snowier-snow akio
     * @reason deeper snow
     */
    @Nullable
    @Overwrite
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return LAYERS_TO_SHAPE[state.get(LAYERS) > 2 ? state.get(LAYERS) - 2 : 0];
    }
}
