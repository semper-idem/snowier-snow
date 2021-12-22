package ss.snowiersnow.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import ss.snowiersnow.block.ModBlocks;
import ss.snowiersnow.client.renderer.SnowierBlockEntityRenderer;
import ss.snowiersnow.SnowierSnow;

public class SnowierSnowClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BlockEntityRendererRegistry.register(ModBlocks.SNOW_BLOCK_ENTITY, SnowierBlockEntityRenderer::new);
    }
}
