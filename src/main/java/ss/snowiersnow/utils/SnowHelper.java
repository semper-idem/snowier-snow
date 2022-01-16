package ss.snowiersnow.utils;

import net.minecraft.block.*;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.block.enums.WallShape;
import net.minecraft.server.world.ServerWorld;
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
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldEvents;
import ss.snowiersnow.block.ModBlocks;
import ss.snowiersnow.blockentity.SnowContentBlockEntity;

import java.util.ArrayList;

public class SnowHelper {
    private static final ArrayList<Block> POSSIBLE_CONTENT = new ArrayList<>();
    private static final ArrayList<Block> BLOCKS_WITH_BASE = new ArrayList<>();
    private static final ArrayList<Block> ALLOW_RANDOM_TICK = new ArrayList<>();
    private static final BlockState DEFAULT_SNOW_STATE = ModBlocks.SNOW_WITH_CONTENT.getDefaultState();
    private static final VoxelShape TALL_POST_SHAPE = Block.createCuboidShape(7.0D, 0.0D, 7.0D, 9.0D, 16.0D, 9.0D);


    public static boolean canContain(BlockState state) {
        return canContain(state.getBlock());
    }

    public static boolean canContain(Block block) {
        return POSSIBLE_CONTENT.contains(block);
    }

    public static SnowContentBlockEntity getBlockEntity(BlockView blockView, BlockPos blockPos) {
        return blockView.getBlockEntity(blockPos, ModBlocks.SNOW_WITH_CONTENT_ENTITY).orElse(null);
    }

    public static BlockState getContentState(BlockView blockView, BlockPos blockPos){
        SnowContentBlockEntity sbe = blockView.getBlockEntity(blockPos, ModBlocks.SNOW_WITH_CONTENT_ENTITY).orElse(null);
        return sbe != null ? sbe.getContent() : Blocks.AIR.getDefaultState();
    }

    public static void setContent(BlockState content, WorldAccess world, BlockPos pos) {
        getBlockEntity(world, pos).setContent(content);
    }

    public static boolean isContentBase(BlockState content) {
        return BLOCKS_WITH_BASE.contains(content.getBlock());
    }

    public static boolean isContentFence(WorldAccess worldAccess, BlockPos pos) {
        return getContentState(worldAccess, pos).isIn(BlockTags.FENCES);
    }

    public static void setOrStackSnow(WorldAccess worldAccess, BlockPos pos) {
        BlockState contentOrSnow = worldAccess.getBlockState(pos);
        if (contentOrSnow.isIn(ModBlocks.SNOW_TAG)) {
            stackSnow(contentOrSnow, worldAccess, pos);
        } else if (canContain(contentOrSnow) || contentOrSnow.isAir()) {
            setSnow(contentOrSnow, worldAccess, pos);
        }
    }
    public static void setOrStackSnow(ServerWorld serverWorld, BlockPos pos) {
        BlockState contentOrSnow = serverWorld.getBlockState(pos);
        if (contentOrSnow.isIn(ModBlocks.SNOW_TAG)) {
            stackSnow(contentOrSnow, serverWorld, pos);
        } else if (canContain(contentOrSnow) || contentOrSnow.isAir())  {
            setSnow(contentOrSnow, serverWorld, pos);
        }
    }

    public static void setSnow(BlockState futureContent, WorldAccess world, BlockPos pos) {
        world.setBlockState(pos, DEFAULT_SNOW_STATE, Block.NOTIFY_LISTENERS, 512);
        if (futureContent.getBlock() instanceof TallPlantBlock && futureContent.get(TallPlantBlock.HALF) == DoubleBlockHalf.LOWER) {
            world.setBlockState(pos.up(), futureContent.with(TallPlantBlock.HALF, DoubleBlockHalf.UPPER), Block.NOTIFY_LISTENERS, 512);
        }
        futureContent.updateNeighbors(world, pos, Block.NOTIFY_NEIGHBORS);
        setContent(futureContent, world, pos);
    }

    public static void setSnow(BlockState futureContent, ServerWorld world, BlockPos pos) {
        world.setBlockState(pos, DEFAULT_SNOW_STATE);
        Block futureContentBlock = futureContent.getBlock();
        if (futureContentBlock instanceof TallPlantBlock && futureContent.get(TallPlantBlock.HALF) == DoubleBlockHalf.LOWER) {
            world.setBlockState(pos.up(), futureContent.with(TallPlantBlock.HALF, DoubleBlockHalf.UPPER));
        } else if (
            futureContentBlock instanceof FenceBlock ||
                futureContentBlock instanceof FenceGateBlock ||
                futureContentBlock instanceof WallBlock
        ) {
            futureContent = getBlockConnectedState(futureContent, world, pos);
            futureContent.updateNeighbors(world, pos, Block.NOTIFY_NEIGHBORS);
        }
        setContent(futureContent, world, pos);
    }

    public static void putInSnow(BlockState futureContent, WorldAccess world, BlockPos pos, int layers) {
        Block futureContentBlock = futureContent.getBlock();
        if (canContain(futureContentBlock)) {
            world.setBlockState(pos, DEFAULT_SNOW_STATE.with(SnowBlock.LAYERS, layers), Block.NOTIFY_LISTENERS, 512);
            if (futureContentBlock instanceof TallPlantBlock && futureContent.get(TallPlantBlock.HALF) == DoubleBlockHalf.LOWER) {
                world.setBlockState(pos.up(), futureContent.with(TallPlantBlock.HALF, DoubleBlockHalf.UPPER), Block.NOTIFY_LISTENERS, 512);
            } else if (
                    futureContentBlock instanceof FenceBlock ||
                    futureContentBlock instanceof FenceGateBlock ||
                    futureContentBlock instanceof WallBlock
            ) {
                futureContent = getBlockConnectedState(futureContent, world, pos);
                futureContent.updateNeighbors(world, pos, Block.NOTIFY_NEIGHBORS);
            }
            setContent(futureContent, world, pos);
        }
    }

    public static void stackSnow(BlockState snowWithContent, WorldAccess worldAccess, BlockPos pos) {
        int currentLayers = snowWithContent.get(SnowBlock.LAYERS);
        if (currentLayers != 8) {
            worldAccess.setBlockState(pos, snowWithContent.with(SnowBlock.LAYERS, currentLayers + 1), Block.NOTIFY_LISTENERS, 512);
        }
        BlockState content = getContentState(worldAccess, pos);
        if (!content.isOf(Blocks.AIR)) {
            content.updateNeighbors(worldAccess, pos, Block.NOTIFY_NEIGHBORS);
        }
    }

    public static void stackSnow(BlockState snowWithContent, ServerWorld serverWorld, BlockPos pos) {
        int currentLayers = snowWithContent.get(SnowBlock.LAYERS);
        if (currentLayers != 8) {
            serverWorld.setBlockState(pos, snowWithContent.with(SnowBlock.LAYERS, currentLayers + 1), Block.NOTIFY_LISTENERS, 512);
        }
        BlockState content = getContentState(serverWorld, pos);
        if (!content.isAir()) {
            content.updateNeighbors(serverWorld, pos, Block.NOTIFY_NEIGHBORS);
        }
    }

    public static void removeOrReduce(BlockState snowState, World world, BlockPos pos) {
        BlockState content = getContentState(world, pos);
            int layers = snowState.get(SnowBlock.LAYERS);
        if (layers == 1) {
            world.getChunk(pos).removeBlockEntity(pos);
            if (!content.isAir()) {
                world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL); //spaghetti for onStateReplaced method
                world.setBlockState(pos, content, Block.NOTIFY_ALL);
                if (content.getBlock() instanceof TallPlantBlock) {
                    world.setBlockState(pos.up(), content.with(TallPlantBlock.HALF, DoubleBlockHalf.UPPER), Block.NOTIFY_ALL);
                }
                content.updateNeighbors(world, pos, Block.NOTIFY_NEIGHBORS);
            } else {
                world.breakBlock(pos, false);
            }
        } else {
            world.setBlockState(pos, snowState.with(SnowBlock.LAYERS, layers - 1), Block.NOTIFY_ALL);
            if (!content.isAir()) {
                content.updateNeighbors(world, pos, Block.NOTIFY_NEIGHBORS);
            }
        }
        world.syncWorldEvent(WorldEvents.BLOCK_BROKEN, pos, Block.getRawIdFromState(Blocks.SNOW.getDefaultState()));
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
        boolean isFence =  state.isOf(block) || getContentState(world, pos).isOf(block);
        return isFence || isSolid;
    }

    public static void playSound(WorldAccess worldAccess, BlockPos pos, SoundEvent soundEvent) {
        if (worldAccess.isClient()) {
            worldAccess.playSound(null, pos, soundEvent, SoundCategory.BLOCKS, BlockSoundGroup.SNOW.volume, BlockSoundGroup.SNOW.pitch);
        }
    }

    public static void addSnowloggableBlock(Block block) {
        POSSIBLE_CONTENT.add(block);
    }

    public static void addBlocksWithBase(Block block) {
        BLOCKS_WITH_BASE.add(block);
    }

    public static void addAllowRandomTick(Block block) {
        ALLOW_RANDOM_TICK.add(block);
    }

    public static boolean isRandomTickAllowed(Block block) {
        return ALLOW_RANDOM_TICK.contains(block);
    }

    public static boolean contentShouldBreak(int layers, BlockState content) {
        if (content.isAir()) {
            return false;
        }
        float contentHardness = content.getBlock().getHardness();
        return layers == 8 ? contentHardness < 0.2F : contentHardness < 0.1F;
    }

}
