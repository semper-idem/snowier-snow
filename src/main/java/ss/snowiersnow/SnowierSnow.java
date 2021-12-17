package ss.snowiersnow;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import ss.snowiersnow.block.SnowierBlock;
import ss.snowiersnow.block.SnowBlockEntity;

public class SnowierSnow implements ModInitializer {
    public static final String MOD_NAME = "Snowier snow";
    private static final String MODID = "snowier-snow";
    private static final Logger logger = LogManager.getLogger(MOD_NAME);
    private final static Identifier SNOW_BLOCK_ID = new Identifier(MODID, "snow");
    public static SnowierBlock SNOW_BLOCK = new SnowierBlock(FabricBlockSettings.copyOf(Blocks.SNOW));
    public static BlockEntityType<SnowBlockEntity> SNOW_BE;
    private static long timer = 0;

    public static void log(Object message){
        logger.info("[" + MOD_NAME + "]: " + message);
    }

    @Override
    public void onInitialize() {
        SNOW_BE = Registry.register(Registry.BLOCK_ENTITY_TYPE, SNOW_BLOCK_ID,
            FabricBlockEntityTypeBuilder.create(SnowBlockEntity::new, SNOW_BLOCK).build(null));
        Registry.register(Registry.BLOCK, SNOW_BLOCK_ID, SNOW_BLOCK);
    }


    public static void slowLog(String msg){
        if (System.currentTimeMillis() - timer > 1000) {
            System.out.println(msg);
            timer = System.currentTimeMillis();
        }
    }
}
