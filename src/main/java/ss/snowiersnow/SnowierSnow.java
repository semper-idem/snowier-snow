package ss.snowiersnow;

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
import ss.snowiersnow.block.DefaultSnowBlock;
import ss.snowiersnow.block.ModBlocks;
import ss.snowiersnow.blockentity.SnowContentBlockEntity;

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
