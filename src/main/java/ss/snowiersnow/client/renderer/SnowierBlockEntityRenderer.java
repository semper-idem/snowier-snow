package ss.snowiersnow.client.renderer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.SnowBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import ss.snowiersnow.block.ISnowVariant;
import ss.snowiersnow.blockentity.SnowContentBlockEntity;

import java.util.Random;

@Environment(EnvType.CLIENT)
public class SnowierBlockEntityRenderer implements BlockEntityRenderer<SnowContentBlockEntity> {
    private static final Random R = new Random();
    public SnowierBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {}

    @Override
    public void render(SnowContentBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (entity.getCachedState().getBlock() instanceof ISnowVariant && entity.getCachedState().get(SnowBlock.LAYERS) < 8) {
            BlockState content = entity.getContent();
            if (!content.isAir()) {
                MinecraftClient.getInstance().getBlockRenderManager().renderBlock(
                    content,
                    entity.getPos(),
                    entity.getWorld(),
                    matrices,
                    vertexConsumers.getBuffer(RenderLayer.getCutout()),
                    false, R);
            }
        }
    }
}
