package ss.snowiersnow.mixin;

import net.minecraft.block.*;
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
import ss.snowiersnow.utils.BiomeHelper;
import ss.snowiersnow.utils.SnowHelper;

import java.util.function.Supplier;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin extends World {

    public ServerWorldMixin(MutableWorldProperties properties, RegistryKey<World> registryRef, DimensionType dimensionType, Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long seed) {
        super(properties, registryRef, dimensionType, profiler, isClient, debugWorld, seed);
    }

    @Inject(
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/biome/Biome;canSetSnow(Lnet/minecraft/world/WorldView;Lnet/minecraft/util/math/BlockPos;)Z",
                    ordinal = 0
            ),
            method = "tickChunk"
    )
    private void beforeCanSetSnow(WorldChunk chunk, int randomTickSpeed, CallbackInfo ci) {
        BlockPos pos = getRandomTopPos(chunk);
        BlockState state = this.getBlockState(pos);
        if (BiomeHelper.canSetSnow(this, pos, state)){
            int layers = 0;
            if (state.getBlock() instanceof SnowBlock) {
                layers = state.get(SnowBlock.LAYERS);
                BlockState blockBelow = chunk.getBlockState(pos.down());
                if (blockBelow.getBlock() instanceof SnowBlock) {
                    layers = layers + 8;
                }
            }
            if (state.isAir() || SnowHelper.canContain(state) || shouldAccumulate(layers)) {
                SnowHelper.setOrStackSnow(chunk, pos);
            }
        }
    }

    private boolean shouldAccumulate(int layers){
        return Math.random() < ( 1f / (1 + (layers * 4)));
    }

    private BlockPos getRandomTopPos(WorldChunk chunk){
        BlockPos randomTopBlock =  this.getTopPosition(
            Heightmap.Type.MOTION_BLOCKING,
            this.getRandomPosInChunk(chunk.getPos().getStartX(), 0, chunk.getPos().getStartZ(), 15));

        //Possible performance hit(untested)
        //TODO test performance, maybe implement new heightmap type, future config option candidate !HashMap BlockState/chance to pass hardcoded or at start
        if (!chunk.getBlockState(randomTopBlock.down()).isFullCube(chunk, randomTopBlock.down())) {
            for (int y = randomTopBlock.getY(); y > chunk.getBottomY(); y--){
                randomTopBlock = randomTopBlock.down();
                BlockState topBlockState = chunk.getBlockState(randomTopBlock);
                Block topBlock = topBlockState.getBlock();
                if(topBlock instanceof StairsBlock || topBlock instanceof SlabBlock || topBlockState.isSolidBlock(chunk, randomTopBlock)) {
                    break;
                }
            }
        }
        return randomTopBlock;
    }
}
