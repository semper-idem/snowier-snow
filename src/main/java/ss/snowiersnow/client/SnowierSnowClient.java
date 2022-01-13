package ss.snowiersnow.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import ss.snowiersnow.block.ModBlocks;
import ss.snowiersnow.client.renderer.SnowBlockEntityRenderer;

public class SnowierSnowClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BlockEntityRendererRegistry.register(ModBlocks.SNOW_WITH_CONTENT_ENTITY, SnowBlockEntityRenderer::new);
    }
}
