package ss.snowiersnow.utils;

import net.minecraft.block.*;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.tag.Tag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldEvents;
import ss.snowiersnow.block.SnowWithContent;
import ss.snowiersnow.block.ModBlocks;
import ss.snowiersnow.blockentity.SnowContentBlockEntity;

import java.util.ArrayList;
import java.util.HashMap;

public class SnowHelper {
    private static final HashMap<Block, SnowWithContent> snowloggableBlock = new HashMap<>();
    private static final ArrayList<Class <?>> mustHaveBase = new ArrayList<>();

    public static boolean canContain(BlockState state) {
        return canContain(state.getBlock());
    }

    public static boolean canContain(Block block) {
        return snowloggableBlock.containsKey(block);
    }

    public static SnowContentBlockEntity getBlockEntity(BlockView blockView, BlockPos blockPos) {
        return blockView.getBlockEntity(blockPos, ModBlocks.SNOW_ENTITY).orElse(null);
    }
    public static BlockState getContentState(BlockView blockView, BlockPos blockPos){
        SnowContentBlockEntity sbe = blockView.getBlockEntity(blockPos, ModBlocks.SNOW_ENTITY).orElse(null);
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
        return mustHaveBase.stream().noneMatch( clazz -> clazz.isInstance(contentBlock));
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
        world.setBlockState(pos, getSnowForContent(futureContent), Block.NOTIFY_LISTENERS, 512);
        if (futureContent.getBlock() instanceof TallPlantBlock && futureContent.get(TallPlantBlock.HALF) == DoubleBlockHalf.LOWER) {
            world.setBlockState(pos.up(), futureContent.with(TallPlantBlock.HALF, DoubleBlockHalf.UPPER), Block.NOTIFY_LISTENERS, 512);
        }
        setContentState(futureContent, world, pos);
    }

    public static void setSnow(BlockState futureContent, ServerWorld world, BlockPos pos) {
        world.setBlockState(pos, getSnowForContent(futureContent));
        if (futureContent.getBlock() instanceof TallPlantBlock && futureContent.get(TallPlantBlock.HALF) == DoubleBlockHalf.LOWER) {
            world.setBlockState(pos.up(), futureContent.with(TallPlantBlock.HALF, DoubleBlockHalf.UPPER));
        }
        setContentState(futureContent, world, pos);
    }

    public static void putInSnow(BlockState futureContent, WorldAccess world, BlockPos pos, int layers) {
        if (canContain(futureContent)) {
            world.setBlockState(pos, getSnowForContent(futureContent).with(SnowWithContent.LAYERS, layers), Block.NOTIFY_LISTENERS, 512);
            if (futureContent.getBlock() instanceof TallPlantBlock && futureContent.get(TallPlantBlock.HALF) == DoubleBlockHalf.LOWER) {
                world.setBlockState(pos.up(), futureContent.with(TallPlantBlock.HALF, DoubleBlockHalf.UPPER), Block.NOTIFY_LISTENERS, 512);
            }
            setContentState(futureContent, world, pos);
        }
    }

    public static void putInSnow(BlockState futureContent, ServerWorld world, BlockPos pos, int layers) {
        if (canContain(futureContent)) {
            world.setBlockState(pos, getSnowForContent(futureContent).with(SnowWithContent.LAYERS, layers));
            if (futureContent.getBlock() instanceof TallPlantBlock && futureContent.get(TallPlantBlock.HALF) == DoubleBlockHalf.LOWER) {
                world.setBlockState(pos.up(), futureContent.with(TallPlantBlock.HALF, DoubleBlockHalf.UPPER));
            }
            setContentState(futureContent, world, pos);
        }
    }

    public static void stackSnow(BlockState snowWithContent, WorldAccess worldAccess, BlockPos pos) {
        int currentLayers = snowWithContent.get(SnowBlock.LAYERS);
        if (currentLayers != 8) {
            worldAccess.setBlockState(pos, snowWithContent.with(SnowWithContent.LAYERS, currentLayers + 1), Block.NOTIFY_LISTENERS, 0);
        }
    }

    public static void stackSnow(BlockState snowWithContent, ServerWorld serverWorld, BlockPos pos) {
        int currentLayers = snowWithContent.get(SnowBlock.LAYERS);
        if (currentLayers != 8) {
            serverWorld.setBlockState(pos, snowWithContent.with(SnowWithContent.LAYERS, currentLayers + 1), Block.NOTIFY_LISTENERS, 0);
        }
    }

    public static void removeOrReduce(BlockState snowState, WorldAccess worldAccess, BlockPos pos) {
        BlockState content = getContentState(worldAccess, pos);
        int layers = snowState.get(SnowWithContent.LAYERS);
        if (layers == 1) {
            if (!content.isAir()) {
                worldAccess.getChunk(pos).removeBlockEntity(pos);
                worldAccess.setBlockState(pos, content, Block.NOTIFY_ALL);
                if (content.getBlock() instanceof TallPlantBlock) {
                    worldAccess.setBlockState(pos.up(), content.with(TallPlantBlock.HALF, DoubleBlockHalf.UPPER), Block.NOTIFY_LISTENERS);
                }
            } else {
                worldAccess.breakBlock(pos, false);
            }
        } else {
            worldAccess.setBlockState(pos, snowState.with(SnowWithContent.LAYERS, layers - 1), Block.NOTIFY_LISTENERS);
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
        mustHaveBase.add(TallPlantBlock.class);
    }

    public static BlockState getSnowForContent(BlockState state) {
        return ((Block)snowloggableBlock.get(state.getBlock())).getDefaultState();
    }

    public static void addSnowloggableBlock(Block block) {
        snowloggableBlock.put(block, ModBlocks.DEFAULT_SNOW);
    }

    public static void addSnowloggableBlockTag(Tag.Identified<Block> blockTag, SnowWithContent snowWithContent) {
        blockTag.values().forEach( block -> addSnowloggableBlock(block, snowWithContent));
    }
    public static void addSnowloggableBlockTag(Tag.Identified<Block> blockTag) {
        blockTag.values().forEach(SnowHelper::addSnowloggableBlock);
    }

    public static void addSnowloggableBlock(Block block, SnowWithContent snowWithContent) {
        snowloggableBlock.put(block, snowWithContent);
    }

    public static boolean contentShouldBreak(int layers, BlockState content) {
        if (content.isAir()) {
            return false;
        }
        float contentHardness = content.getBlock().getHardness();
        return layers == 8 ? contentHardness < 0.2F : contentHardness < 0.1F;
    }
}
