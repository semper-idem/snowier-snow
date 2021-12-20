package ss.snowiersnow.block;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.SnowBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.world.World;

import java.util.Random;

@Environment(EnvType.CLIENT)
public class SnowierBlockEntityRenderer implements BlockEntityRenderer<SnowierBlockEntity> {
    private static final Random R = new Random();
    public SnowierBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {}

    @Override
    public void render(SnowierBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (entity.getCachedState().getBlock() instanceof ISnowierBlock && entity.getCachedState().get(SnowBlock.LAYERS) < 8) {
            BlockState content = entity.getContentState();
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
