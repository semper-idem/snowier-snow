package ss.snowiersnow.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SnowBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.*;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ss.snowiersnow.state.SnowBlockState;

import java.util.function.Supplier;

@Mixin(ServerWorld.class)
public abstract class MixinServerWorld extends World {

    public MixinServerWorld(MutableWorldProperties properties, RegistryKey<World> registryRef, DimensionType dimensionType, Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long seed) {
        super(properties, registryRef, dimensionType, profiler, isClient, debugWorld, seed);
    }

    @Inject(
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/biome/Biome;canSetSnow(Lnet/minecraft/world/WorldView;Lnet/minecraft/util/math/BlockPos;)Z",
                    ordinal = 0
            ),
            method = "tickChunk")
    private void beforeCanSetSnow(WorldChunk chunk, int randomTickSpeed, CallbackInfo ci) {
        BlockPos pos = getRandomTopPos(chunk);
        BlockState blockState = this.getBlockState(pos);
        if (canSetSnow(this, pos, blockState)){
            snow(pos, blockState);
        }
    }

    private boolean canSetSnow(WorldView world, BlockPos pos,  BlockState blockState) {
        if (!world.getBiome(pos).doesNotSnow(pos)) {
            if (pos.getY() >= world.getBottomY() &&
                pos.getY() < world.getTopY() &&
                world.getLightLevel(LightType.BLOCK, pos) < 10)
            {
                return (blockState.isOf(Blocks.SNOW) || SnowBlockState.contains(blockState)) && Blocks.SNOW.getDefaultState().canPlaceAt(world, pos);
            }
        }
        return false;
    }

    private BlockPos getRandomTopPos(WorldChunk chunk){
        BlockPos randomTopBlock =  this.getTopPosition(
            Heightmap.Type.MOTION_BLOCKING,
            this.getRandomPosInChunk(chunk.getPos().getStartX(), 0, chunk.getPos().getStartZ(), 15)
        );
        if (SnowBlockState.contains(chunk.getBlockState(randomTopBlock.down()))) {
            return randomTopBlock.down();
        }
        return randomTopBlock;
    }

    //performance hit
    private void snow(BlockPos pos, BlockState blockState){
        if(blockState.getBlock() == Blocks.SNOW) {
            this.setBlockState(pos, blockState.with(SnowBlock.LAYERS, getNewLayers(blockState.get(SnowBlock.LAYERS))));
        } else {
           this.setBlockState(pos, Blocks.SNOW.getDefaultState()
               .with(SnowBlockState.CONTENT, SnowBlockState.get(blockState)));
        }
    }

    private int getNewLayers(int currentLayers) {
        if(currentLayers < 6 && shouldAddLayer(currentLayers)) {
            return currentLayers + 1;
        }
        return currentLayers;
    }

    private boolean shouldAddLayer(int layers){
        return Math.random() < ( 1f / (1 + (layers * 4))); // 20 > 15 > 10 > 5 > 0
    }
}
