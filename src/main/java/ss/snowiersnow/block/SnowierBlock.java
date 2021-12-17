package ss.snowiersnow.block;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.*;
import org.jetbrains.annotations.Nullable;
import ss.snowiersnow.SnowierSnow;

import java.util.Optional;
import java.util.Random;

public class SnowierBlock extends SnowBlock implements BlockEntityProvider {


    public SnowierBlock(Settings settings) {
        super(settings);
    }

    public void setContent(WorldView world, BlockPos pos, BlockState content){
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof SnowBlockEntity) {
            ((SnowBlockEntity) blockEntity).setContent(content);
        }
    }



    public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
        switch(type) {
            case LAND:
                return (Integer)state.get(LAYERS) < 5;
            case WATER:
                return false;
            case AIR:
                return false;
            default:
                return false;
        }
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        Optional<SnowBlockEntity> optionalContent = world.getBlockEntity(pos, SnowierSnow.SNOW_BE);
        if (optionalContent.isPresent()) {
            if (!optionalContent.get().isEmpty()){
                return VoxelShapes.union(LAYERS_TO_SHAPE[state.get(LAYERS)],
                    optionalContent.get().getContent().getOutlineShape(world, pos, context));
            }
        }
        return LAYERS_TO_SHAPE[state.get(LAYERS)];
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        int layers = state.get(LAYERS);
        Optional<SnowBlockEntity> optionalContent = world.getBlockEntity(pos, SnowierSnow.SNOW_BE);
        if (optionalContent.isPresent()) {
            if (!optionalContent.get().isEmpty()){
                return VoxelShapes.union(LAYERS_TO_SHAPE[layers < 2 ? 0 : layers - 2],
                    optionalContent.get().getContent().getOutlineShape(world, pos, context));
            }
        }
        return LAYERS_TO_SHAPE[layers < 2 ? 0 : layers - 2];
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        BlockState blockState = world.getBlockState(pos.down());
        if (!blockState.isOf(Blocks.ICE) && !blockState.isOf(Blocks.PACKED_ICE) && !blockState.isOf(Blocks.BARRIER)) {
            if (!blockState.isOf(Blocks.HONEY_BLOCK) && !blockState.isOf(Blocks.SOUL_SAND)) {
                return Block.isFaceFullSquare(blockState.getCollisionShape(world, pos.down()), Direction.UP) || blockState.isOf(this) && (Integer)blockState.get(LAYERS) == 8;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (world.getLightLevel(LightType.BLOCK, pos) > 11) {
            dropStacks(state, world, pos);
            world.removeBlock(pos, false);
        }

    }


    @Override
    public boolean canReplace(BlockState state, ItemPlacementContext context) {
        int i = state.get(LAYERS);
        if (context.getStack().isOf(this.asItem()) && i < 8) {
            if (context.canReplaceExisting()) {
                return context.getSide() == Direction.UP;
            } else {
                return true;
            }
        } else {
            return i == 1;
        }
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new SnowBlockEntity(pos, state);
    }
}
