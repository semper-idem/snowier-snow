package ss.snowiersnow;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.RenderLayer;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import ss.snowiersnow.state.SnowBlockState;

public class SnowierSnow implements ModInitializer {
    public static final String MOD_NAME = "Snowier snow";
    private static final String MODID = "snowier-snow";
    private static final Logger logger = LogManager.getLogger(MOD_NAME);

    public static void log(Object message){
        logger.info("[" + MOD_NAME + "]: " + message);
    }

    @Override
    public void onInitialize() {
        SnowBlockState.setBlockStates();
        BlockRenderLayerMap.INSTANCE.putBlock(Blocks.SNOW, RenderLayer.getCutout());
    }
}
