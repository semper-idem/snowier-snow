package ss.snowiersnow.mixin.block;

import net.minecraft.block.BambooBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
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
    private final static IntProperty AGE = BambooBlock.AGE;

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
        if (state.get(AGE) == 1) {
            pos = pos.down(3);
            BlockState blockState = world.getBlockState(pos);
            if (blockState.isOf(ModBlocks.SNOW_WITH_CONTENT)) {
                updateLeavesInSnow(world, pos);
            } else {
                pos = pos.down();
                blockState = world.getBlockState(pos);
                if (blockState.isOf(ModBlocks.SNOW_WITH_CONTENT)) {
                    updateLeavesInSnow(world, pos);
                }
            }
        }
    }

    private void updateLeavesInSnow( World world, BlockPos pos){
        BlockState state = ContentBlockEntity.getContent(world, pos);
        if (state.isOf(Blocks.BAMBOO)) {
            ContentBlockEntity.setContent((this.getDefaultState().with(AGE, state.get(AGE))), world, pos);
        }
    }
}
