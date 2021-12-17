package ss.snowiersnow.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SnowyBlock;
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
import ss.snowiersnow.SnowierSnow;
import ss.snowiersnow.block.SnowBlockHelper;
import ss.snowiersnow.block.Snowloggable;

@Mixin(FreezeTopLayerFeature.class)
public class MixinFreezeTopLayerFeature {

    /**
     * @author snowier-snow akio
     */
    @Overwrite
    public boolean generate(FeatureContext<DefaultFeatureConfig> context) {
        StructureWorldAccess structureWorldAccess = context.getWorld();
        BlockPos blockPos = context.getOrigin();
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        BlockPos.Mutable mutable2 = new BlockPos.Mutable();

        //NOT TESTED AT ALL
        for(int i = 0; i < 16; ++i) {
            for(int j = 0; j < 16; ++j) {
                int k = blockPos.getX() + i;
                int l = blockPos.getZ() + j;
                int m = structureWorldAccess.getTopY(Heightmap.Type.MOTION_BLOCKING, k, l);
                mutable.set(k, m, l);
                mutable2.set(mutable).move(Direction.DOWN, 1);
                Biome biome = structureWorldAccess.getBiome(mutable);
                if (biome.canSetIce(structureWorldAccess, mutable2, false)) {
                    structureWorldAccess.setBlockState(mutable2, Blocks.ICE.getDefaultState(), Block.NOTIFY_LISTENERS);
                }

                BlockState blockState = structureWorldAccess.getBlockState(mutable2);
                if (SnowBlockHelper.canSetSnow(structureWorldAccess, mutable, blockState)) {
                    structureWorldAccess.setBlockState(mutable2, SnowierSnow.SNOW_BLOCK.getDefaultState(), Block.NOTIFY_LISTENERS);
                    if (Snowloggable.contains(blockState)) {
                        SnowierSnow.SNOW_BLOCK.setContent(structureWorldAccess, mutable2, blockState);
                    }
                    if (blockState.contains(SnowyBlock.SNOWY)) {
                        structureWorldAccess.setBlockState(mutable2, (BlockState)blockState.with(SnowyBlock.SNOWY, true), Block.NOTIFY_LISTENERS);
                    }
                }

            }
        }
        return true;
    }
}
