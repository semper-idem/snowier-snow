package ss.snowiersnow.utils;

import net.minecraft.block.*;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.Tag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldEvents;
import ss.snowiersnow.block.ModBlocks;
import ss.snowiersnow.blockentity.SnowContentBlockEntity;
import java.util.ArrayList;

public class SnowHelper {
    private static final ArrayList<Block> POSSIBLE_CONTENT = new ArrayList<>();
    private static final ArrayList<Class <?>> BLOCKS_WITH_BASE = new ArrayList<>();
    private static final BlockState DEFAULT_SNOW_STATE = ModBlocks.SNOW_WITH_CONTENT.getDefaultState();


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
    public static void setContentState(BlockState content, WorldAccess world, BlockPos pos) {
        getBlockEntity(world, pos).setContent(content);
    }

    public static void setContentState(BlockState content, ServerWorld world, BlockPos pos) {
        getBlockEntity(world, pos).setContent(content);
    }

    public static boolean isContentBase(BlockState content) {
        Block contentBlock = content.getBlock();
        return BLOCKS_WITH_BASE.stream().noneMatch(clazz -> clazz.isInstance(contentBlock));
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
        setContentState(futureContent, world, pos);
    }

    public static void setSnow(BlockState futureContent, ServerWorld world, BlockPos pos) {
        world.setBlockState(pos, DEFAULT_SNOW_STATE);
        if (futureContent.getBlock() instanceof TallPlantBlock && futureContent.get(TallPlantBlock.HALF) == DoubleBlockHalf.LOWER) {
            world.setBlockState(pos.up(), futureContent.with(TallPlantBlock.HALF, DoubleBlockHalf.UPPER));
        }
        setContentState(futureContent, world, pos);
    }

    public static void putInSnow(BlockState futureContent, WorldAccess world, BlockPos pos, int layers) {
        if (canContain(futureContent)) {
            world.setBlockState(pos, DEFAULT_SNOW_STATE.with(SnowBlock.LAYERS, layers), Block.NOTIFY_LISTENERS, 512);
            if (futureContent.getBlock() instanceof TallPlantBlock && futureContent.get(TallPlantBlock.HALF) == DoubleBlockHalf.LOWER) {
                world.setBlockState(pos.up(), futureContent.with(TallPlantBlock.HALF, DoubleBlockHalf.UPPER), Block.NOTIFY_LISTENERS, 512);
            } else if (futureContent.isIn(BlockTags.FENCES)) {
                futureContent = fencePlacementState(futureContent, world, pos);
                futureContent.updateNeighbors(world, pos, Block.NOTIFY_NEIGHBORS);
            }
            setContentState(futureContent, world, pos);
        }
    }

    public static void putInSnow(BlockState futureContent, ServerWorld world, BlockPos pos, int layers) {
        if (canContain(futureContent)) {
            world.setBlockState(pos, DEFAULT_SNOW_STATE.with(SnowBlock.LAYERS, layers));
            if (futureContent.getBlock() instanceof TallPlantBlock && futureContent.get(TallPlantBlock.HALF) == DoubleBlockHalf.LOWER) {
                world.setBlockState(pos.up(), futureContent.with(TallPlantBlock.HALF, DoubleBlockHalf.UPPER));
            }
            setContentState(futureContent, world, pos);
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

    private static BlockState fencePlacementState(BlockState fence, WorldAccess world, BlockPos pos){
        boolean connectedNorth = connectedTo(world, pos.north(), Direction.SOUTH);
        boolean connectedSouth = connectedTo(world, pos.south(), Direction.NORTH);
        boolean connectedEast = connectedTo(world, pos.east(), Direction.WEST);
        boolean connectedWest = connectedTo(world, pos.west(), Direction.EAST);
        return fence
            .with(HorizontalConnectingBlock.NORTH, connectedNorth)
            .with(HorizontalConnectingBlock.SOUTH, connectedSouth)
            .with(HorizontalConnectingBlock.EAST, connectedEast)
            .with(HorizontalConnectingBlock.WEST, connectedWest);
    }

    private static boolean connectedTo(WorldAccess world, BlockPos pos, Direction from) {
        BlockState state = world.getBlockState(pos);
        boolean isSolid = state.isSideSolidFullSquare(world, pos, from);
        boolean isFence =  state.isIn(BlockTags.FENCES) || getContentState(world, pos).isIn(BlockTags.FENCES);
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

    public static void addSnowloggableBlockTag(Tag.Identified<Block> blockTag) {
        blockTag.values().forEach(SnowHelper::addSnowloggableBlock);
    }

    public static boolean contentShouldBreak(int layers, BlockState content) {
        if (content.isAir()) {
            return false;
        }
        float contentHardness = content.getBlock().getHardness();
        return layers == 8 ? contentHardness < 0.2F : contentHardness < 0.1F;
    }

    static {
        BLOCKS_WITH_BASE.add(SugarCaneBlock.class);
        BLOCKS_WITH_BASE.add(BambooBlock.class);
        BLOCKS_WITH_BASE.add(TallPlantBlock.class);
    }
}
