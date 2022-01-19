package ss.snowiersnow.utils;

import net.minecraft.block.*;
import net.minecraft.block.enums.WallShape;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import ss.snowiersnow.blockentity.ContentBlockEntity;
import ss.snowiersnow.registry.ModBlocks;
import ss.snowiersnow.registry.ModTags;


public class ConnectingBlockHelper {
    public static final BooleanProperty UP;
    public static final BooleanProperty NORTH;
    public static final BooleanProperty EAST;
    public static final BooleanProperty SOUTH;
    public static final BooleanProperty WEST;
    public static final EnumProperty<WallShape> EAST_SHAPE;
    public static final EnumProperty<WallShape> NORTH_SHAPE;
    public static final EnumProperty<WallShape> SOUTH_SHAPE;
    public static final EnumProperty<WallShape> WEST_SHAPE;
    public static final BooleanProperty WATERLOGGED;
    private static final VoxelShape TALL_POST_SHAPE;
    private static final VoxelShape TALL_NORTH_SHAPE;
    private static final VoxelShape TALL_SOUTH_SHAPE;
    private static final VoxelShape TALL_WEST_SHAPE;
    private static final VoxelShape TALL_EAST_SHAPE;

    public static boolean isConnectingBlockState(BlockState blockState) {
        return
            blockState.isIn(BlockTags.FENCES) ||
            blockState.isIn(BlockTags.WALLS) ||
            blockState.getBlock() instanceof PaneBlock;
    }

    public static BlockState getConnectingBlockState(BlockState connectingBlockState, WorldAccess world, BlockPos pos) {
        if (connectingBlockState.isIn(BlockTags.FENCES)) {
            connectingBlockState = ConnectingBlockHelper.getFenceConnectingBlock(connectingBlockState, world, pos);
        } else if (connectingBlockState.isIn(BlockTags.WALLS)) {
            connectingBlockState = ConnectingBlockHelper.getWallConnectingBlock(connectingBlockState, world, pos);
        } else if (connectingBlockState.getBlock() instanceof PaneBlock) {
            connectingBlockState = ConnectingBlockHelper.getPaneConnectingBlock(connectingBlockState, world, pos);
        }
        return connectingBlockState;
    }


    public static BlockState getFenceConnectingBlock(BlockState fenceState, WorldAccess world, BlockPos pos) {
        boolean north = isFenceConnective(world, pos.north(), Direction.SOUTH);
        boolean south = isFenceConnective(world, pos.south(), Direction.NORTH);
        boolean west = isFenceConnective(world, pos.west(), Direction.EAST);
        boolean east = isFenceConnective(world, pos.east(), Direction.WEST);
        return fenceState.with(NORTH, north).with(SOUTH, south).with(EAST, east).with(WEST, west);
    }

    public static BlockState getPaneConnectingBlock(BlockState paneState, WorldAccess world, BlockPos pos) {
        boolean north = isPaneConnective(world, pos.north(), Direction.SOUTH);
        boolean south = isPaneConnective(world, pos.south(), Direction.NORTH);
        boolean west = isPaneConnective(world, pos.west(), Direction.EAST);
        boolean east = isPaneConnective(world, pos.east(), Direction.WEST);
        return paneState.with(NORTH, north).with(SOUTH, south).with(EAST, east).with(WEST, west);
    }


    public static BlockState getWallConnectingBlock(BlockState wallState, WorldAccess world, BlockPos pos) {
        boolean north = isWallConnective(world, pos.north(), Direction.SOUTH);
        boolean south = isWallConnective(world, pos.south(), Direction.NORTH);
        boolean west = isWallConnective(world, pos.west(), Direction.EAST);
        boolean east = isWallConnective(world, pos.east(), Direction.WEST);
        BlockState aboveState = world.getBlockState(pos.up());
        VoxelShape aboveShape = aboveState.getCollisionShape(world, pos).getFace(Direction.DOWN);
        return getWallWith(wallState, north, east, south, west, aboveShape)
            .with(UP, shouldHavePost(wallState, aboveState, aboveShape));
    }

    public static boolean isFenceConnective(BlockView world, BlockPos pos, Direction direction) {
        BlockState connectsTo = world.getBlockState(pos);
        if (connectsTo.isOf(ModBlocks.SNOW_WITH_CONTENT)) {
            BlockState content = ContentBlockEntity.getContent(world, pos);
            if (content.isIn(BlockTags.FENCES)) {
                connectsTo = content;
            }
        }
        boolean canConnect = !Block.cannotConnect(connectsTo);
        boolean isSolid = connectsTo.isSideSolidFullSquare(world, pos ,direction);
        boolean isFence = connectsTo.isIn(BlockTags.FENCES);
        return canConnect && (isSolid || isFence);
    }

    public static boolean isPaneConnective(BlockView world, BlockPos pos, Direction direction) {
        BlockState connectsTo = world.getBlockState(pos);
        if (connectsTo.isIn(ModTags.SNOW_BLOCK_TAG)) {
            BlockState content = ContentBlockEntity.getContent(world, pos);
            if (content.getBlock() instanceof PaneBlock || content.isIn(BlockTags.WALLS)) {
                connectsTo = content;
            }
        }
        boolean canConnect = !Block.cannotConnect(connectsTo);
        boolean isSolid = connectsTo.isSideSolidFullSquare(world, pos ,direction);
        boolean isPane = connectsTo.getBlock() instanceof PaneBlock;
        boolean isWall = connectsTo.isIn(BlockTags.WALLS);
        return canConnect && (isSolid || isPane || isWall);
    }

    public static boolean isWallConnective(BlockView world, BlockPos pos, Direction direction) {
        BlockState connectsTo = world.getBlockState(pos);
        if (connectsTo.isOf(ModBlocks.SNOW_WITH_CONTENT)) {
            BlockState content = ContentBlockEntity.getContent(world, pos);
            if (content.getBlock() instanceof PaneBlock || content.isIn(BlockTags.WALLS)) {
                connectsTo = content;
            }
        }
        Block connectsToBlock = connectsTo.getBlock();
        boolean canConnect = !Block.cannotConnect(connectsTo);
        boolean isSolid = connectsTo.isSideSolidFullSquare(world, pos ,direction);
        boolean isWall = connectsTo.isIn(BlockTags.WALLS);
        boolean isPane = connectsToBlock instanceof PaneBlock;
        boolean isGate = connectsToBlock instanceof FenceGateBlock && FenceGateBlock.canWallConnect(connectsTo, direction);
        return canConnect && (isSolid || isPane || isWall || isGate);
    }

    private static boolean shouldHavePost(BlockState state, BlockState aboveState, VoxelShape aboveShape) {
        boolean bl = aboveState.getBlock() instanceof WallBlock && aboveState.get(UP);
        if (bl) {
            return true;
        } else {
            WallShape wallShape = state.get(NORTH_SHAPE);
            WallShape wallShape2 = state.get(SOUTH_SHAPE);
            WallShape wallShape3 = state.get(EAST_SHAPE);
            WallShape wallShape4 = state.get(WEST_SHAPE);
            boolean bl2 = wallShape2 == WallShape.NONE;
            boolean bl3 = wallShape4 == WallShape.NONE;
            boolean bl4 = wallShape3 == WallShape.NONE;
            boolean bl5 = wallShape == WallShape.NONE;
            boolean bl6 = bl5 && bl2 && bl3 && bl4 || bl5 != bl2 || bl3 != bl4;
            if (bl6) {
                return true;
            } else {
                boolean bl7 = wallShape == WallShape.TALL && wallShape2 == WallShape.TALL || wallShape3 == WallShape.TALL && wallShape4 == WallShape.TALL;
                if (bl7) {
                    return false;
                } else {
                    return aboveState.isIn(BlockTags.WALL_POST_OVERRIDE) || shouldUseTallShape(aboveShape, TALL_POST_SHAPE);
                }
            }
        }
    }

    private static BlockState getWallWith(BlockState state, boolean north, boolean east, boolean south, boolean west, VoxelShape aboveShape) {
        return state
            .with(WallBlock.NORTH_SHAPE, getWallShape(north, aboveShape, TALL_NORTH_SHAPE))
            .with(EAST_SHAPE, getWallShape(east, aboveShape, TALL_EAST_SHAPE))
            .with(SOUTH_SHAPE, getWallShape(south, aboveShape, TALL_SOUTH_SHAPE))
            .with(WEST_SHAPE, getWallShape(west, aboveShape, TALL_WEST_SHAPE));
    }

    private static WallShape getWallShape(boolean connected, VoxelShape aboveShape, VoxelShape tallShape) {
        if (connected) {
            return shouldUseTallShape(aboveShape, tallShape) ? WallShape.TALL : WallShape.LOW;
        } else {
            return WallShape.NONE;
        }
    }

    private static boolean shouldUseTallShape(VoxelShape aboveShape, VoxelShape tallShape) {
        return !VoxelShapes.matchesAnywhere(tallShape, aboveShape, BooleanBiFunction.ONLY_FIRST);
    }

    static {
        UP = Properties.UP;
        EAST_SHAPE = Properties.EAST_WALL_SHAPE;
        NORTH_SHAPE = Properties.NORTH_WALL_SHAPE;
        SOUTH_SHAPE = Properties.SOUTH_WALL_SHAPE;
        WEST_SHAPE = Properties.WEST_WALL_SHAPE;
        WATERLOGGED = Properties.WATERLOGGED;
        NORTH = Properties.NORTH;
        EAST = Properties.EAST;
        SOUTH = Properties.SOUTH;
        WEST = Properties.WEST;
        TALL_POST_SHAPE = Block.createCuboidShape(7.0D, 0.0D, 7.0D, 9.0D, 16.0D, 9.0D);
        TALL_NORTH_SHAPE = Block.createCuboidShape(7.0D, 0.0D, 0.0D, 9.0D, 16.0D, 9.0D);
        TALL_SOUTH_SHAPE = Block.createCuboidShape(7.0D, 0.0D, 7.0D, 9.0D, 16.0D, 16.0D);
        TALL_WEST_SHAPE = Block.createCuboidShape(0.0D, 0.0D, 7.0D, 9.0D, 16.0D, 9.0D);
        TALL_EAST_SHAPE = Block.createCuboidShape(7.0D, 0.0D, 7.0D, 16.0D, 16.0D, 9.0D);
    }
}