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
    public final static Identifier DEFAULT_SNOW_ID = new Identifier(SnowierSnow.MODID, "snow");
    public static DefaultSnowBlock DEFAULT_SNOW;
    public static BlockEntityType<SnowContentBlockEntity> SNOW_ENTITY;

    static {
        DEFAULT_SNOW = new DefaultSnowBlock(FabricBlockSettings.copy(Blocks.SNOW));
        SNOW_ENTITY = FabricBlockEntityTypeBuilder.create(SnowContentBlockEntity::new, DEFAULT_SNOW).build(null);
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
                block instanceof SaplingBlock ||
                block instanceof TallFlowerBlock ||
                 block instanceof FenceBlock)
            {
                SnowHelper.addSnowloggableBlock(block);
            }
        });

        Registry.register(Registry.BLOCK, DEFAULT_SNOW_ID, DEFAULT_SNOW);
        Registry.register(Registry.BLOCK_ENTITY_TYPE, DEFAULT_SNOW_ID, SNOW_ENTITY);
        Registry.register(Registry.ITEM, DEFAULT_SNOW_ID, new BlockItem(DEFAULT_SNOW, new FabricItemSettings().group(ItemGroup.DECORATIONS)));
    }
}
