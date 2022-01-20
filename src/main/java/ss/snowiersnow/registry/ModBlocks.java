package ss.snowiersnow.registry;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import ss.snowiersnow.SnowierSnow;
import ss.snowiersnow.block.SnowWithContentBlock;
import ss.snowiersnow.blockentity.ContentBlockEntity;

public class ModBlocks {
    public final static Identifier SNOW_WITH_CONTENT_ID = new Identifier(SnowierSnow.MODID, "snow");
    public static SnowWithContentBlock SNOW_WITH_CONTENT;
    public static BlockEntityType<ContentBlockEntity> CONTENT_ENTITY;

    static {
        SNOW_WITH_CONTENT = new SnowWithContentBlock(FabricBlockSettings.copy(Blocks.SNOW));
        CONTENT_ENTITY = FabricBlockEntityTypeBuilder.create(ContentBlockEntity::new, SNOW_WITH_CONTENT).build(null);
    }

    public static void register() {
        Registry.register(Registry.BLOCK, SNOW_WITH_CONTENT_ID, SNOW_WITH_CONTENT);
        Registry.register(Registry.BLOCK_ENTITY_TYPE, SNOW_WITH_CONTENT_ID, CONTENT_ENTITY);
        Registry.register(Registry.ITEM, SNOW_WITH_CONTENT_ID, new BlockItem(SNOW_WITH_CONTENT, new FabricItemSettings().group(ItemGroup.MISC)));
    }
}
