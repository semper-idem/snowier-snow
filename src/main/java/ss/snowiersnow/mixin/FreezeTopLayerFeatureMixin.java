package ss.snowiersnow.mixin;

import net.minecraft.block.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.Heightmap;
import net.minecraft.world.LightType;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.FreezeTopLayerFeature;
import net.minecraft.world.gen.feature.util.FeatureContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import ss.snowiersnow.registry.ModBlocks;
import ss.snowiersnow.utils.SnowHelper;
import ss.snowiersnow.utils.Snowloggable;

@Mixin(FreezeTopLayerFeature.class)
public class FreezeTopLayerFeatureMixin {
    /**
     * @author snowier-snow akio
     */
    @Overwrite
    public boolean generate(FeatureContext<DefaultFeatureConfig> context) {
        StructureWorldAccess structureWorldAccess = context.getWorld();
        BlockPos blockPos = context.getOrigin();
        BlockPos.Mutable onSurfacePos = new BlockPos.Mutable();
        BlockPos.Mutable surfacePos = new BlockPos.Mutable();
        BlockPos.Mutable onSurfacePosNoLeaves = new BlockPos.Mutable();
        BlockPos.Mutable surfacePosNoLeaves = new BlockPos.Mutable();

        for(int chunkX = 0; chunkX < 16; ++chunkX) {
            for(int chunkZ = 0; chunkZ < 16; ++chunkZ) {
                int x = blockPos.getX() + chunkX;
                int z = blockPos.getZ() + chunkZ;
                int y = structureWorldAccess.getTopY(Heightmap.Type.MOTION_BLOCKING, x, z);
                int yNoLeaves = structureWorldAccess.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x, z);
                if (y != yNoLeaves) {
                    onSurfacePos.set(x, y, z);
                    surfacePos.set(onSurfacePos).move(Direction.DOWN, 1);
                    setSnowOnTree(structureWorldAccess.getBiome(onSurfacePos), structureWorldAccess, onSurfacePos, surfacePos);
                }
                onSurfacePosNoLeaves.set(x, yNoLeaves, z);
                surfacePosNoLeaves.set(onSurfacePosNoLeaves).move(Direction.DOWN, 1);
                freeze(structureWorldAccess.getBiome(onSurfacePosNoLeaves), structureWorldAccess, onSurfacePosNoLeaves, surfacePosNoLeaves);
            }
        }
        return true;
    }

    private void setSnowOnTree(Biome biome, StructureWorldAccess worldAccess, BlockPos onSurfacePos, BlockPos surfacePos){
        if (canSnowOnTree(biome, worldAccess, onSurfacePos)) {
            worldAccess.setBlockState(onSurfacePos, Blocks.SNOW.getDefaultState(), Block.NOTIFY_LISTENERS);
        }
    }

    private boolean canSnowOnTree(Biome biome, StructureWorldAccess worldAccess, BlockPos pos) {
        return !biome.doesNotSnow(pos)
            && worldAccess.getLightLevel(LightType.BLOCK, pos) < 10
            && worldAccess.getBlockState(pos.down()).getBlock() instanceof LeavesBlock;
    }

    private void freeze(Biome biome, StructureWorldAccess worldAccess, BlockPos onSurfacePos, BlockPos surfacePos){
        if (biome.canSetIce(worldAccess, surfacePos, false)) {
            worldAccess.setBlockState(surfacePos, Blocks.ICE.getDefaultState(), Block.NOTIFY_LISTENERS);
        }
        else if (canSnow(biome, worldAccess, onSurfacePos)) {
                setSnow(worldAccess, onSurfacePos);
                setSnowy(worldAccess, surfacePos);
        }
    }

    private boolean canSnow(Biome biome, StructureWorldAccess worldAccess, BlockPos pos) {
        BlockState blockState = worldAccess.getBlockState(pos);
        return !biome.doesNotSnow(pos)
            && worldAccess.getLightLevel(LightType.BLOCK, pos) < 10
            && ModBlocks.SNOW_WITH_CONTENT.canPlaceAt(null, worldAccess, pos)
            && (blockState.isAir() || Snowloggable.canSnowContain(blockState));
    }

    private void setSnow(StructureWorldAccess worldAccess, BlockPos pos) {
        BlockState state = worldAccess.getBlockState(pos);
        if (!state.isAir()){
            SnowHelper.putInFeature(state, worldAccess, pos);
        } else {
            worldAccess.setBlockState(pos, Blocks.SNOW.getDefaultState(), Block.NOTIFY_LISTENERS);
        }
    }

    private void setSnowy(StructureWorldAccess worldAccess, BlockPos pos) {
        BlockState surfaceState = worldAccess.getBlockState(pos);
        if (surfaceState.contains(SnowyBlock.SNOWY)) {
            worldAccess.setBlockState(pos, surfaceState.with(SnowyBlock.SNOWY, true), Block.NOTIFY_LISTENERS);
        }
    }
}
