package ss.snowiersnow.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SnowBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.Heightmap;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ss.snowiersnow.registry.ModTags;
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
    private void beforeCanSetSnow(WorldChunk world, int randomTickSpeed, CallbackInfo ci) {
        BlockPos pos = getRandomTopPos(world).up();
        BlockState stateAboveSurface = this.getBlockState(pos);
        if (SnowHelper.canAddSnowLayer(stateAboveSurface, this, pos )){
            if (shouldAccumulate(calculateSnowLayers(stateAboveSurface, world, pos))) {
                if (stateAboveSurface.isIn(ModTags.SNOW_BLOCK_TAG)) {
                    this.setBlockState(pos, stateAboveSurface.with(SnowBlock.LAYERS, stateAboveSurface.get(SnowBlock.LAYERS) + 1));
                } else if (stateAboveSurface.isAir()) {
                    this.setBlockState(pos, Blocks.SNOW.getDefaultState());
                } else {
                    SnowHelper.putIn(Blocks.SNOW.getDefaultState(), stateAboveSurface, this, pos);
                }
            }
        }
    }

    private int calculateSnowLayers(BlockState state, WorldChunk world, BlockPos pos) {
        int layers = 0;
        if (state.isIn(ModTags.SNOW_BLOCK_TAG)) {
            layers = state.get(SnowBlock.LAYERS);
            BlockState blockBelow = world.getBlockState(pos.down());
            if (blockBelow.getBlock() instanceof SnowBlock) {
                layers = layers + 8;
            }
        }
        return layers;
    }

    private boolean shouldAccumulate(int layers){
        return Math.random() < ( 1f / (1 + (layers * 4)));
    }

    private BlockPos getRandomTopPos(WorldChunk world){
        BlockPos pos =  this.getTopPosition(
            Heightmap.Type.MOTION_BLOCKING,
            this.getRandomPosInChunk(world.getPos().getStartX(), 0, world.getPos().getStartZ(), 15)
        );
        BlockState surface = this.getBlockState(pos);
        while (pos.getY() > world.getBottomY() && !isBlockStateBlocking(world, pos, surface)) {
            pos = pos.down();
            surface = this.getBlockState(pos);
        }
        return pos;
    }

    private boolean isBlockStateBlocking(WorldChunk world, BlockPos pos, BlockState state) {
        return state.isSideSolidFullSquare(world ,pos, Direction.UP) || state.isSideSolidFullSquare(world ,pos, Direction.UP);
    }
}
