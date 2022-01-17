package ss.snowiersnow.mixin.block;


import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.SnowBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import ss.snowiersnow.registry.ModTags;
import ss.snowiersnow.utils.SnowHelper;


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
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return LAYERS_TO_SHAPE[state.get(LAYERS) > 2 ? state.get(LAYERS) - 2 : 0];
    }


    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        BlockState stateInHand = Block.getBlockFromItem(player.getStackInHand(hand).getItem()).getDefaultState();
        if (stateInHand.isIn(ModTags.SNOWLOGGABLE_TAG)) {
            SnowHelper.putInSnow(stateInHand, world, pos, state.get(LAYERS));
            return ActionResult.success(world.isClient);
        }
        return ActionResult.PASS;
    }
}
