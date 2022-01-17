package ss.snowiersnow;

import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import ss.snowiersnow.registry.ModBlocks;

public class SnowierSnow implements ModInitializer {
    public static final String MOD_NAME = "Snowier snow";
    public static final String MODID = "snowier-snow";
    private static final Logger logger = LogManager.getLogger(MOD_NAME);

    public static void log(Object message){
        logger.info("[" + MOD_NAME + "]: " + message);
    }

    @Override
    public void onInitialize() {
        ModBlocks.register();
    }
}
