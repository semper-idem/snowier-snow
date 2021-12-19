package ss.snowiersnow.block;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;

import java.util.Random;

public class SnowierBlockEntityRenderer implements BlockEntityRenderer<SnowierBlockEntity> {

    public SnowierBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {}

    @Override
    public void render(SnowierBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        BlockState content = entity.getContent();
        if (!content.isAir()) {
            MinecraftClient.getInstance().getBlockRenderManager().renderBlock(content, entity.getPos(), entity.getWorld(), matrices, vertexConsumers.getBuffer(RenderLayer.getCutout()),false, new Random());
        }
    }
}
