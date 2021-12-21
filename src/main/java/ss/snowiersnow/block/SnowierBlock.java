package ss.snowiersnow.block;

import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.*;
import ss.snowiersnow.initializers.SnowierSnow;

import java.util.Optional;
import java.util.Random;

@SuppressWarnings("deprecation")
public class SnowierBlock extends SnowBlock implements ISnowierBlock {
    public SnowierBlock(Settings settings) {
        super(settings);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return ISnowierBlock.super.getCollisionShape(state, world, pos, context);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return  ISnowierBlock.super.getOutlineShape(state, world,pos, context);
    }

    @Override
    public VoxelShape getSidesShape(BlockState state, BlockView world, BlockPos pos) {
        return  ISnowierBlock.super.getSidesShape(state, world,pos);
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        BlockState blockState = world.getBlockState(pos.down());
        if (!blockState.isOf(Blocks.ICE) && !blockState.isOf(Blocks.PACKED_ICE) && !blockState.isOf(Blocks.BARRIER)) {
            if (!blockState.isOf(Blocks.HONEY_BLOCK) && !blockState.isOf(Blocks.SOUL_SAND)) {
                return Block.isFaceFullSquare(blockState.getCollisionShape(world, pos.down()), Direction.UP) || blockState.isOf(this) && blockState.get(ISnowierBlock.LAYERS) == 8;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        ISnowierBlock.super.randomTick(state, world, pos, random);
    }

    @Override
    public boolean canReplace(BlockState state, ItemPlacementContext context) {
        BlockState content = getContentState(context.getWorld(),state, context.getBlockPos());
        if (content.getBlock().asItem() == context.getStack().getItem()) {
            return content.getBlock().canReplace(content, context);
        }
        int i = state.get(ISnowierBlock.LAYERS);
        if (context.getStack().isOf(this.asItem()) && i < 8) {
            if (context.canReplaceExisting() && state.getBlock() == SnowierSnow.SNOW_BLOCK) {
                return context.getSide() == Direction.UP;
            } else {
                return true;
            }
        } else {
            return i <= 2;
        }
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock())) {
            Optional<SnowierBlockEntity> snowierBlockEntity = world.getBlockEntity(pos, SnowierSnow.SNOW_BE);
            if (snowierBlockEntity.isPresent()) {
                BlockState content = snowierBlockEntity.get().getContentState();
                if (!content.isAir()) {
                    if (content.getBlock() instanceof TallPlantBlock) {
                        world.setBlockState(pos.up(), Blocks.AIR.getDefaultState());
                    }
                    if (content.getHardness(world, pos) < state.getHardness(world, pos)) {
                        ItemScatterer.spawn(world, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(content.getBlock()));
                        snowierBlockEntity.get().clear();
                        if (state.canPlaceAt(world, pos)) {
                            world.setBlockState(pos, state);
                        }
                    } else {
                        if (content.canPlaceAt(world, pos)) {
                            world.setBlockState(pos, content);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onLandedUpon(World world, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        ISnowierBlock.super.onLandedUpon(world, state, pos, entity, fallDistance);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        return ISnowierBlock.super.onUse(state, world, pos, player, hand, hit);
    }

    @Override
    public float calcBlockBreakingDelta(BlockState state, PlayerEntity player, BlockView world, BlockPos pos) {
        return ISnowierBlock.super.calcBlockBreakingDelta(state, player, world, pos);
    }

}
