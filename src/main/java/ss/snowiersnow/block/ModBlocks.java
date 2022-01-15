package ss.snowiersnow.block;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.tag.TagFactory;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import ss.snowiersnow.SnowierSnow;
import ss.snowiersnow.blockentity.SnowContentBlockEntity;
import ss.snowiersnow.utils.SnowHelper;

public class ModBlocks {
    public static final Tag<Block> SNOW_TAG = TagFactory.BLOCK.create(new Identifier("snowier-snow", "snow"));
    public final static Identifier SNOW_WITH_CONTENT_ID = new Identifier(SnowierSnow.MODID, "snow");
    public static SnowWithContentBlock SNOW_WITH_CONTENT;
    public static BlockEntityType<SnowContentBlockEntity> SNOW_WITH_CONTENT_ENTITY;

    static {
        SNOW_WITH_CONTENT = new SnowWithContentBlock(FabricBlockSettings.copy(Blocks.SNOW));
        SNOW_WITH_CONTENT_ENTITY = FabricBlockEntityTypeBuilder.create(SnowContentBlockEntity::new, SNOW_WITH_CONTENT).build(null);
    }

    public static void register() {
        SnowHelper.addSnowloggableBlock(Blocks.BAMBOO);
        SnowHelper.addSnowloggableBlock(Blocks.SUGAR_CANE);
        SnowHelper.addSnowloggableBlock(Blocks.SWEET_BERRY_BUSH);
        SnowHelper.addSnowloggableBlock(Blocks.GRASS);
        SnowHelper.addSnowloggableBlock(Blocks.FERN);
        SnowHelper.addSnowloggableBlock(Blocks.TALL_GRASS);
        SnowHelper.addSnowloggableBlock(Blocks.LARGE_FERN);
        SnowHelper.addSnowloggableBlock(Blocks.SWEET_BERRY_BUSH);

        Registry.BLOCK.forEach( block -> {
            if (block instanceof FlowerBlock ||
                 block instanceof FenceBlock) {
                SnowHelper.addSnowloggableBlock(block);
            }

            if (block instanceof TallPlantBlock) {
                SnowHelper.addSnowloggableBlock(block);
                SnowHelper.addBlocksWithBase(block);
            }

            if (block instanceof SaplingBlock) {
                SnowHelper.addSnowloggableBlock(block);
                SnowHelper.addAllowRandomTick(block);
            }
        });

        SnowHelper.addBlocksWithBase(Blocks.SUGAR_CANE);
        SnowHelper.addBlocksWithBase(Blocks.BAMBOO);
        SnowHelper.addAllowRandomTick(Blocks.SWEET_BERRY_BUSH);

        Registry.register(Registry.BLOCK, SNOW_WITH_CONTENT_ID, SNOW_WITH_CONTENT);
        Registry.register(Registry.BLOCK_ENTITY_TYPE, SNOW_WITH_CONTENT_ID, SNOW_WITH_CONTENT_ENTITY);
        Registry.register(Registry.ITEM, SNOW_WITH_CONTENT_ID, new BlockItem(SNOW_WITH_CONTENT, new FabricItemSettings().group(ItemGroup.DECORATIONS)));
    }
}
