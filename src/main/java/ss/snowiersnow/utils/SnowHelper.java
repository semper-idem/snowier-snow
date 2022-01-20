package ss.snowiersnow.utils;

import net.minecraft.block.*;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldEvents;
import ss.snowiersnow.blockentity.ContentBlockEntity;
import ss.snowiersnow.registry.ModBlocks;

import static ss.snowiersnow.utils.Snowloggable.canSnowContain;

public class SnowHelper {

    public static void putIn(BlockState snowState, BlockState content, WorldAccess world, BlockPos pos) {
        if (canSnowContain(content)) {
            world.setBlockState(pos, ModBlocks.SNOW_WITH_CONTENT.getDefaultState().with(SnowBlock.LAYERS, snowState.get(SnowBlock.LAYERS)), Block.NOTIFY_ALL);
            if (content.getBlock() instanceof TallPlantBlock) {
                world.setBlockState(pos.up(), content.with(TallPlantBlock.HALF, DoubleBlockHalf.UPPER), Block.NOTIFY_ALL);
            } else if (ConnectingBlockHelper.isConnectingBlockState(content)) {
                content = ConnectingBlockHelper.getConnectingBlockState(content, world, pos);
            }
            updateSelfAndNeighbors(content, world, pos);
        }
    }

    public static void putInFeature(BlockState content, WorldAccess world, BlockPos pos) {
        if (canSnowContain(content)) {
            world.setBlockState(pos, ModBlocks.SNOW_WITH_CONTENT.getDefaultState(), Block.NOTIFY_LISTENERS);
            ContentBlockEntity.setContent(content, world, pos);
            if (content.getBlock() instanceof TallPlantBlock) {
                world.setBlockState(pos.up(), content.with(TallPlantBlock.HALF, DoubleBlockHalf.UPPER), Block.NOTIFY_ALL);
            }
        }
    }

    private static void updateSelfAndNeighbors(BlockState content, WorldAccess world, BlockPos pos){
        ContentBlockEntity.setContent(content, world, pos);
        if (!(content.getBlock() instanceof TallPlantBlock)) {
            content.updateNeighbors(world, pos, Block.NOTIFY_ALL);
        }
    }

    public static void meltLayer(BlockState snowState, World world, BlockPos pos) {
        BlockState contentState = ContentBlockEntity.getContent(world, pos);
        int currentLayers = snowState.get(SnowBlock.LAYERS);
        if (currentLayers > 1) {
            decrementSnowLayer(snowState, contentState, world, pos, currentLayers);
        } else {
            removeSnowLayer(contentState, world, pos);
        }
    }

    private static void removeSnowLayer(BlockState contentState,  World world, BlockPos pos) {
        world.removeBlockEntity(pos);
        //world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL); //spaghetti for onStateReplaced method
        world.setBlockState(pos, contentState, Block.NOTIFY_ALL);
        if (contentState.getBlock() instanceof TallPlantBlock) {
            world.setBlockState(pos.up(), contentState.with(TallPlantBlock.HALF, DoubleBlockHalf.UPPER), Block.NOTIFY_ALL);
        }
    }

    private static void decrementSnowLayer(BlockState snowState, BlockState contentState,  World world, BlockPos pos, int currentLayers) {
        world.setBlockState(pos, snowState.with(SnowBlock.LAYERS, currentLayers - 1), Block.NOTIFY_ALL);
        contentState.updateNeighbors(world, pos, Block.NOTIFY_NEIGHBORS);
        world.syncWorldEvent(WorldEvents.BLOCK_BROKEN, pos, Block.getRawIdFromState(Blocks.SNOW.getDefaultState()));
    }

    public static void playSound(WorldAccess worldAccess, BlockPos pos, SoundEvent soundEvent) {
        if (worldAccess.isClient()) {
            worldAccess.playSound(null, pos, soundEvent, SoundCategory.BLOCKS, BlockSoundGroup.SNOW.volume, BlockSoundGroup.SNOW.pitch);
        }
    }

}
