package ss.snowiersnow.initializers;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import ss.snowiersnow.block.SnowierBlockEntityRenderer;
import ss.snowiersnow.initializers.SnowierSnow;

public class SnowierSnowClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BlockEntityRendererRegistry.register(SnowierSnow.SNOW_BE, SnowierBlockEntityRenderer::new);
    }
}
