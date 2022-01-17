package ss.snowiersnow.utils;

import net.minecraft.block.*;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.block.enums.WallShape;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.state.property.Properties;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldEvents;
import ss.snowiersnow.blockentity.ContentBlockEntity;
import ss.snowiersnow.registry.ModBlocks;
import ss.snowiersnow.registry.ModTags;

public class SnowHelper {
    private static final VoxelShape TALL_POST_SHAPE = Block.createCuboidShape(7.0D, 0.0D, 7.0D, 9.0D, 16.0D, 9.0D);

    public static void addLayer(BlockState content, WorldAccess world, BlockPos pos) {
        world.setBlockState(pos, ModBlocks.SNOW_WITH_CONTENT.getDefaultState(), Block.NOTIFY_LISTENERS, 512);
        if (content.getBlock() instanceof TallPlantBlock && content.get(TallPlantBlock.HALF) == DoubleBlockHalf.LOWER) {
            world.setBlockState(pos.up(), content.with(TallPlantBlock.HALF, DoubleBlockHalf.UPPER), Block.NOTIFY_LISTENERS, 512);
        } else if (isConnectingBlock(content)) {
            content = getBlockConnectedState(content, world, pos);
            content.updateNeighbors(world, pos, Block.NOTIFY_NEIGHBORS);
        }
        ContentBlockEntity.setContent(content, world, pos);
    }


    public static void meltLayer(BlockState snowState, World world, BlockPos pos) {
        BlockState content = ContentBlockEntity.getContent(world, pos);
        int layers = snowState.get(SnowBlock.LAYERS);
        if (layers == 1) {
            world.getChunk(pos).removeBlockEntity(pos);
            world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL); //spaghetti for onStateReplaced method
            world.setBlockState(pos, content, Block.NOTIFY_ALL);
            if (content.getBlock() instanceof TallPlantBlock) {
                world.setBlockState(pos.up(), content.with(TallPlantBlock.HALF, DoubleBlockHalf.UPPER), Block.NOTIFY_ALL);
            }
        } else {
            world.setBlockState(pos, snowState.with(SnowBlock.LAYERS, layers - 1), Block.NOTIFY_ALL);
        }
        content.updateNeighbors(world, pos, Block.NOTIFY_NEIGHBORS);
        world.syncWorldEvent(WorldEvents.BLOCK_BROKEN, pos, Block.getRawIdFromState(Blocks.SNOW.getDefaultState()));
    }

    public static void putInSnow(BlockState futureContent, WorldAccess world, BlockPos pos, int layers) {
        if (futureContent.isIn(ModTags.SNOWLOGGABLE_TAG)) {
            Block futureContentBlock = futureContent.getBlock();
            world.setBlockState(pos, ModBlocks.SNOW_WITH_CONTENT.getDefaultState().with(SnowBlock.LAYERS, layers), Block.NOTIFY_LISTENERS, 512);
            if (futureContentBlock instanceof TallPlantBlock && futureContent.get(TallPlantBlock.HALF) == DoubleBlockHalf.LOWER) {
                world.setBlockState(pos.up(), futureContent.with(TallPlantBlock.HALF, DoubleBlockHalf.UPPER), Block.NOTIFY_LISTENERS, 512);
            } else if (futureContent.isIn(BlockTags.FENCES)) {
                futureContent = getBlockConnectedState(futureContent, world, pos);
                futureContent.updateNeighbors(world, pos, Block.NOTIFY_NEIGHBORS);
            }
            ContentBlockEntity.setContent(futureContent, world, pos);
        }
    }

    private static boolean isConnectingBlock(BlockState state) {
        return state.isIn(BlockTags.FENCES) || state.isIn(BlockTags.FENCE_GATES) || state.isIn(BlockTags.WALLS);
    }


    private static BlockState getBlockConnectedState(BlockState connectingBlock, WorldAccess world, BlockPos pos){
        Block block = connectingBlock.getBlock();
        if (block instanceof FenceBlock) {
            return getFenceBlockState(connectingBlock, world, pos, block);
        } else {
            return getWallBlockState(connectingBlock, world, pos, block);
        }
    }

    private static BlockState getWallBlockState(BlockState blockState, WorldAccess world, BlockPos pos, Block block) {
        boolean connectedNorth = connectedTo(world, pos.north(), Direction.SOUTH, block);
        boolean connectedSouth = connectedTo(world, pos.south(), Direction.NORTH, block);
        boolean connectedEast = connectedTo(world, pos.east(), Direction.WEST, block);
        boolean connectedWest = connectedTo(world, pos.west(), Direction.EAST, block);
        BlockState aboveState = world.getBlockState(pos.up());
        BlockState blockState2 = blockState
            .with(WallBlock.NORTH_SHAPE, connectedNorth ? WallShape.LOW : WallShape.NONE)
            .with(WallBlock.SOUTH_SHAPE, connectedSouth ? WallShape.LOW : WallShape.NONE)
            .with(WallBlock.EAST_SHAPE, connectedEast ? WallShape.LOW : WallShape.NONE)
            .with(WallBlock.WEST_SHAPE, connectedWest ? WallShape.LOW : WallShape.NONE);
        return blockState2
            .with(Properties.UP, shouldHavePost(connectedSouth, connectedWest, connectedEast, connectedNorth, aboveState, aboveState.getCollisionShape(world, pos.up()).getFace(Direction.DOWN)));
    }

    private static boolean shouldHavePost(boolean south, boolean west, boolean east, boolean north, BlockState aboveState, VoxelShape aboveShape) {
        boolean bl = aboveState.getBlock() instanceof WallBlock && (Boolean)aboveState.get(Properties.UP);
        if (bl) {
            return true;
        } else {
            boolean bl6 = south && north && west && east || south != north || west != east;
            if (bl6) {
                return true;
            } else {
				if (north && south || east && west) {
					return false;
				} else {
					return aboveState.isIn(BlockTags.WALL_POST_OVERRIDE) || shouldUseTallShape(aboveShape);
				}
            }
        }
    }

    private static boolean shouldUseTallShape(VoxelShape aboveShape) {
        return !VoxelShapes.matchesAnywhere(SnowHelper.TALL_POST_SHAPE, aboveShape, BooleanBiFunction.ONLY_FIRST);
    }

    private static BlockState getFenceBlockState(BlockState connectingBlock, WorldAccess world, BlockPos pos, Block block){
        boolean connectedNorth = connectedTo(world, pos.north(), Direction.SOUTH, block);
        boolean connectedSouth = connectedTo(world, pos.south(), Direction.NORTH, block);
        boolean connectedEast = connectedTo(world, pos.east(), Direction.WEST, block);
        boolean connectedWest = connectedTo(world, pos.west(), Direction.EAST, block);
        return connectingBlock
            .with(Properties.NORTH, connectedNorth)
            .with(Properties.SOUTH, connectedSouth)
            .with(Properties.EAST, connectedEast)
            .with(Properties.WEST, connectedWest);
    }

    private static boolean connectedTo(WorldAccess world, BlockPos pos, Direction from, Block block) {
        BlockState state = world.getBlockState(pos);
        boolean isSolid = state.isSideSolidFullSquare(world, pos, from);
        boolean isFence =  state.isOf(block) || ContentBlockEntity.getContent(world, pos).isOf(block);
        return isFence || isSolid;
    }

    public static void playSound(WorldAccess worldAccess, BlockPos pos, SoundEvent soundEvent) {
        if (worldAccess.isClient()) {
            worldAccess.playSound(null, pos, soundEvent, SoundCategory.BLOCKS, BlockSoundGroup.SNOW.volume, BlockSoundGroup.SNOW.pitch);
        }
    }
}
