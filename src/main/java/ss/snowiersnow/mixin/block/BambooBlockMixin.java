package ss.snowiersnow.mixin.block;

import net.minecraft.block.BambooBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.enums.BambooLeaves;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ss.snowiersnow.blockentity.ContentBlockEntity;
import ss.snowiersnow.registry.ModBlocks;

import java.util.Random;

@Mixin(BambooBlock.class)
public class BambooBlockMixin extends Block {
    private final static EnumProperty<BambooLeaves> LEAVES = BambooBlock.LEAVES;
    private final static IntProperty AGE = BambooBlock.AGE;
    private final static IntProperty STAGE = BambooBlock.STAGE;

    public BambooBlockMixin(Settings settings) {
        super(settings);
    }

    @Inject(method = "canPlaceAt", at=@At("TAIL"), cancellable = true)
    public void onCanPlaceAt(BlockState state, WorldView world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(
            world.getBlockState(pos.down()).isIn(BlockTags.BAMBOO_PLANTABLE_ON)
                || ContentBlockEntity.getContent(world, pos.down()).isIn(BlockTags.BAMBOO_PLANTABLE_ON));
    }

    @Inject(method = "updateLeaves", at = @At("TAIL"))
    public void updateLeaves(BlockState state, World world, BlockPos pos, Random random, int height, CallbackInfo ci) {
        pos = pos.down(4);
        BlockState blockState = world.getBlockState(pos);
        if (blockState.isOf(ModBlocks.SNOW_WITH_CONTENT)) {
            blockState = ContentBlockEntity.getContent(world, pos);
            if (blockState.isOf(Blocks.BAMBOO)) {
                BambooLeaves bambooLeaves = BambooLeaves.NONE;
                int j = (height < 11 || !(random.nextFloat() < 0.25F)) && height != 15 ? 0 : 1;
                ContentBlockEntity.setContent((this.getDefaultState().with(AGE, state.get(AGE))).with(LEAVES, bambooLeaves).with(STAGE, j), world, pos);
            }
        }
    }
}
