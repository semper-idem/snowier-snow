package ss.snowiersnow.client.renderer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.SnowBlock;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import ss.snowiersnow.blockentity.ContentBlockEntity;

import java.util.Random;

@Environment(EnvType.CLIENT)
public class SnowBlockEntityRenderer implements BlockEntityRenderer<ContentBlockEntity> {
    private static final Random R = new Random();
    private final BlockRenderManager RENDER_MANAGER;
    public SnowBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        RENDER_MANAGER = ctx.getRenderManager();
    }

    @Override
    public void render(ContentBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (entity.getCachedState().get(SnowBlock.LAYERS) < 8) {
            BlockState content = entity.getContent();
        
            if (!content.isAir()) {
                RENDER_MANAGER.renderBlock(
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
