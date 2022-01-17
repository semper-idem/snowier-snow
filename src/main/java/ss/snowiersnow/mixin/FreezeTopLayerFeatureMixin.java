package ss.snowiersnow.mixin;

import net.minecraft.block.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.FreezeTopLayerFeature;
import net.minecraft.world.gen.feature.util.FeatureContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import ss.snowiersnow.utils.BiomeHelper;
import ss.snowiersnow.utils.SnowHelper;

@Mixin(FreezeTopLayerFeature.class)
public class FreezeTopLayerFeatureMixin {

    /**
     * @author snowier-snow akio
     */
    @Overwrite
    public boolean generate(FeatureContext<DefaultFeatureConfig> context) {
        StructureWorldAccess structureWorldAccess = context.getWorld();
        BlockPos blockPos = context.getOrigin();
        BlockPos.Mutable surfacePosLeaves = new BlockPos.Mutable();
        BlockPos.Mutable floorPosLeaves = new BlockPos.Mutable();
        BlockPos.Mutable surfacePosNoLeaves = new BlockPos.Mutable();
        BlockPos.Mutable floorPosNoLeaves = new BlockPos.Mutable();

        for(int i = 0; i < 16; ++i) {
            for(int j = 0; j < 16; ++j) {
                int k = blockPos.getX() + i;
                int l = blockPos.getZ() + j;
                int m = structureWorldAccess.getTopY(Heightmap.Type.MOTION_BLOCKING, k, l);
                int m2 = structureWorldAccess.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, k, l);
                surfacePosLeaves.set(k, m, l);
                floorPosLeaves.set(surfacePosLeaves).move(Direction.DOWN, 1);
                surfacePosNoLeaves.set(k, m2, l);
                floorPosNoLeaves.set(surfacePosNoLeaves).move(Direction.DOWN, 1);
                Biome biome = structureWorldAccess.getBiome(surfacePosLeaves);

                setSnow(biome, structureWorldAccess, surfacePosLeaves, floorPosLeaves);
                if (surfacePosLeaves.getY() != surfacePosNoLeaves.getY()) {
                    setSnow(biome, structureWorldAccess, surfacePosNoLeaves, floorPosNoLeaves);
                }

            }
        }
        return true;
    }

    private void setSnow(Biome biome, StructureWorldAccess worldAccess, BlockPos surfacePos, BlockPos floorPos){
        if (biome.canSetIce(worldAccess, floorPos, false)) {
            worldAccess.setBlockState(floorPos, Blocks.ICE.getDefaultState(), Block.NOTIFY_LISTENERS);
        }

        BlockState possibleContent = worldAccess.getBlockState(surfacePos);
        if (BiomeHelper.canSetSnow(possibleContent, worldAccess, surfacePos)) {
            SnowHelper.addLayer(possibleContent, worldAccess, surfacePos);
            BlockState blockState = worldAccess.getBlockState(floorPos);
            if (blockState.contains(SnowyBlock.SNOWY)) {
                worldAccess.setBlockState(floorPos, blockState.with(SnowyBlock.SNOWY, true), Block.NOTIFY_LISTENERS);
            }
        }
    }
}
