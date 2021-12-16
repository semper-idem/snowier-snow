package ss.snowiersnow.mixin;

import net.fabricmc.fabric.impl.client.indigo.renderer.render.TerrainRenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ss.snowiersnow.state.SnowBlockState;

@Mixin(TerrainRenderContext.class)
public abstract class MixinTerrainRenderContext {

    @Shadow public abstract boolean tesselateBlock(BlockState blockState, BlockPos blockPos, BakedModel model, MatrixStack matrixStack);

    @Inject( method = "tesselateBlock", at = @At("HEAD"))
    public void tesselateBlock(BlockState blockState, BlockPos blockPos, final BakedModel model, MatrixStack matrixStack, CallbackInfoReturnable<Boolean> cir) {
        if (blockState.isOf(Blocks.SNOW)) {
            BlockState content = SnowBlockState.getContent(blockState);
            if (!content.isAir()) {
                matrixStack.push();
                Vec3d vec3d = content.getModelOffset(null, blockPos);
                matrixStack.translate(vec3d.x, vec3d.y, vec3d.z);
                this.tesselateBlock(content, blockPos,
                    MinecraftClient.getInstance().getBakedModelManager().getBlockModels().getModel(content), matrixStack);
                matrixStack.pop();
            }
        }
    }
}
