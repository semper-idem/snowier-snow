package ss.snowiersnow.initializers;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.tag.TagFactory;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.block.SnowBlock;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import ss.snowiersnow.block.SnowierBlock;
import ss.snowiersnow.block.SnowierBlockEntity;

public class SnowierSnow implements ModInitializer {
    public static final String MOD_NAME = "Snowier snow";
    private static final String MODID = "snowier-snow";
    private static final Logger logger = LogManager.getLogger(MOD_NAME);
    public final static Identifier SNOW_BLOCK_ID = new Identifier(MODID, "snow");
    public static SnowierBlock SNOW_BLOCK = new SnowierBlock(FabricBlockSettings.of(Material.SNOW_LAYER).ticksRandomly().strength(0.1F).requiresTool().sounds(BlockSoundGroup.SNOW).blockVision((state, world, pos) -> state.get(SnowBlock.LAYERS) >= 8));
    public static BlockEntityType<SnowierBlockEntity> SNOW_BE;
    public static final Tag<Block> SNOW_BLOCK_TAG = TagFactory.BLOCK.create(SNOW_BLOCK_ID);
    private static long timer = 0;

    public static void log(Object message){
        logger.info("[" + MOD_NAME + "]: " + message);
    }

    @Override
    public void onInitialize() {
        SNOW_BE = Registry.register(Registry.BLOCK_ENTITY_TYPE, SNOW_BLOCK_ID,
            FabricBlockEntityTypeBuilder.create(SnowierBlockEntity::new, SNOW_BLOCK).build(null));
        Registry.register(Registry.BLOCK, SNOW_BLOCK_ID, SNOW_BLOCK);
        Registry.register(Registry.ITEM, SNOW_BLOCK_ID, new BlockItem(SNOW_BLOCK, new FabricItemSettings().group(ItemGroup.MISC)));
    }


    public static void slowLog(String msg){
        if (System.currentTimeMillis() - timer > 1000) {
            System.out.println(msg);
            timer = System.currentTimeMillis();
        }
    }
}
