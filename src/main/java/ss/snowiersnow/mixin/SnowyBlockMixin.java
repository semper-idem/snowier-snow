package ss.snowiersnow.mixin;


import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SnowyBlock;
import net.minecraft.tag.BlockTags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import ss.snowiersnow.block.ModBlocks;

@Mixin(SnowyBlock.class)
public class SnowyBlockMixin extends Block {


    public SnowyBlockMixin(Settings settings) {
        super(settings);
    }

    /**
     * @author snowier-snow si
     */
    @Overwrite
    private static boolean isSnow(BlockState state) {
        System.out.println(state);
        System.out.println(state.isIn(ModBlocks.SNOWIER_SNOW));
        return state.isIn(BlockTags.SNOW) || state.isIn(ModBlocks.SNOWIER_SNOW);
    }
}
