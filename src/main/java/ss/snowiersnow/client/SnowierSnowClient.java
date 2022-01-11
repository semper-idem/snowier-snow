package ss.snowiersnow.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import ss.snowiersnow.block.ModBlocks;
import ss.snowiersnow.client.renderer.SnowierBlockEntityRenderer;

public class SnowierSnowClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BlockEntityRendererRegistry.register(ModBlocks.SNOW_ENTITY, SnowierBlockEntityRenderer::new);
    }
}
