package ss.snowiersnow.utils;

import net.minecraft.block.*;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.chunk.WorldChunk;
import ss.snowiersnow.block.ISnowVariant;
import ss.snowiersnow.block.ModBlocks;
import ss.snowiersnow.blockentity.SnowContentBlockEntity;

import java.util.ArrayList;
import java.util.HashMap;

public class SnowHelper {
    private static final HashMap<Block, ISnowVariant> snowloggableBlock = new HashMap<>();
    private static final ArrayList<Class <?>> mustHaveBase = new ArrayList<>();

    public static boolean canContain(BlockState state) {
        return canContain(state.getBlock());
    }

    public static boolean canContain(Block block) {
        return snowloggableBlock.containsKey(block);
    }

    public static SnowContentBlockEntity getBlockEntity(BlockView blockView, BlockPos blockPos) {
        return blockView.getBlockEntity(blockPos, ModBlocks.SNOW_BLOCK_ENTITY).orElse(null);
    }
    public static BlockState getContentState(BlockView blockView, BlockPos blockPos){
        SnowContentBlockEntity sbe = blockView.getBlockEntity(blockPos, ModBlocks.SNOW_BLOCK_ENTITY).orElse(null);
        return sbe != null ? sbe.getContent() : Blocks.AIR.getDefaultState();
    }
    public static void setContentState(BlockState content, WorldAccess world, BlockPos pos) {
        getBlockEntity(world, pos).setContent(content);
    }

    public static void setContentState(BlockState content, WorldChunk world, BlockPos pos) {
        getBlockEntity(world, pos).setContent(content);
    }

    public static boolean isContentBase(BlockState content) {
        Block contentBlock = content.getBlock();
        return mustHaveBase.stream().noneMatch( clazz -> clazz.isInstance(contentBlock));
    }

    public static void setOrStackSnow(WorldAccess worldAccess, BlockPos pos) {
        BlockState possibleContent = worldAccess.getBlockState(pos);
        if (possibleContent instanceof ISnowVariant) {
            stackSnow(possibleContent, worldAccess, pos);
        } else if (canContain(possibleContent)) {
            setSnow(possibleContent, worldAccess, pos);
        }
    }
    public static void setOrStackSnow(WorldChunk worldChunk, BlockPos pos) {
        BlockState possibleContent = worldChunk.getBlockState(pos);
        if (possibleContent instanceof ISnowVariant) {
            stackSnow(possibleContent, worldChunk, pos);
        } else if (canContain(possibleContent) || possibleContent.isAir())  {
            setSnow(possibleContent, worldChunk, pos);
        }
    }

    public static void setSnow(BlockState content, WorldAccess world, BlockPos pos) {
        world.setBlockState(pos, ModBlocks.SNOW_BLOCK.getDefaultState(), Block.NOTIFY_LISTENERS, 512);
        setContentState(content, world, pos);
    }

    public static void setSnow(BlockState content, WorldChunk worldChunk, BlockPos pos) {
        worldChunk.setBlockState(pos, ModBlocks.SNOW_BLOCK.getDefaultState(), false);
        setContentState(content, worldChunk, pos);
    }
    public static void stackSnow(BlockState state, WorldAccess worldAccess, BlockPos pos) {
        int currentLayers = state.get(ISnowVariant.LAYERS);
        if (currentLayers != 8) {
            worldAccess.setBlockState(pos, state.with(ISnowVariant.LAYERS, currentLayers + 1), Block.NOTIFY_LISTENERS);
        }
    }
    public static void stackSnow(BlockState state, WorldChunk worldChunk, BlockPos pos) {
        int currentLayers = state.get(ISnowVariant.LAYERS);
        if (currentLayers != 8) {
            worldChunk.setBlockState(pos, state.with(ISnowVariant.LAYERS, currentLayers + 1), false);
        }
    }

    public static void removeOrReduce(BlockState snowState, WorldAccess worldAccess, BlockPos pos) {
        BlockState content = getContentState(worldAccess, pos);
        int layers = snowState.get(ISnowVariant.LAYERS);
        if (layers == 1) {
            worldAccess.breakBlock(pos, false);
            if (!content.isAir()) {
                worldAccess.setBlockState(pos, content, Block.NOTIFY_ALL);
            }
        } else {
            worldAccess.setBlockState(pos, snowState.with(ISnowVariant.LAYERS, layers - 1), Block.NOTIFY_ALL);
            worldAccess.syncWorldEvent(WorldEvents.BLOCK_BROKEN, pos, Block.getRawIdFromState(Blocks.SNOW.getDefaultState()));
        }
    }

    public static void playSound(WorldAccess worldAccess, BlockPos pos, SoundEvent soundEvent) {
        if (worldAccess.isClient()) {
            worldAccess.playSound(null, pos, soundEvent, SoundCategory.BLOCKS, BlockSoundGroup.SNOW.volume, BlockSoundGroup.SNOW.pitch);
        }
    }

    static {
        mustHaveBase.add(SugarCaneBlock.class);
        mustHaveBase.add(BambooBlock.class);
    }

    public static void addBlock(Block block) {
        snowloggableBlock.put(block, ModBlocks.SNOW_BLOCK);
    }

    public static void addBlock(Block block, ISnowVariant snowVariant) {
        snowloggableBlock.put(block, snowVariant);
    }

    public static boolean contentShouldBreak(int layers, BlockState content) {
        float contentHardness = content.getBlock().getHardness();
        return layers == 8 ? contentHardness < 0.2F : contentHardness < 0.1F;
    }
}
