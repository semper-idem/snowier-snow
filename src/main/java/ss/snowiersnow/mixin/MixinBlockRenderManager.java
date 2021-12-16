package ss.snowiersnow.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockRenderView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ss.snowiersnow.state.SnowBlockState;

import java.util.Random;

@Mixin(BlockRenderManager.class)
public abstract class MixinBlockRenderManager {


    @Shadow public abstract boolean renderBlock(BlockState state, BlockPos pos, BlockRenderView world, MatrixStack matrices, VertexConsumer vertexConsumer, boolean cull, Random random);

    @Inject(method = "renderBlock", at = @At("HEAD"))
    public void renderBlock(BlockState blockState, BlockPos blockPos, BlockRenderView world, MatrixStack matrixStack, VertexConsumer vertexConsumer, boolean cull, Random random, CallbackInfoReturnable<Boolean> cir){
        if (blockState.isOf(Blocks.SNOW)) {
            BlockState content = SnowBlockState.getContent(blockState);
            if (!content.isAir()) {
                matrixStack.push();
                Vec3d vec3d = content.getModelOffset(null, blockPos);
                matrixStack.translate(vec3d.x, vec3d.y, vec3d.z);
                this.renderBlock(content, blockPos, world, matrixStack, vertexConsumer,cull, random);
                matrixStack.pop();
            }
        }
    }
}
