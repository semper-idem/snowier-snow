package ss.snowiersnow;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import ss.snowiersnow.block.SnowBlockEntityRenderer;

public class SnowierSnowClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BlockEntityRendererRegistry.register(SnowierSnow.SNOW_BE, SnowBlockEntityRenderer::new);
    }
}
