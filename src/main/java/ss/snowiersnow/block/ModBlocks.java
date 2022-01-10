package ss.snowiersnow.block;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.tag.TagFactory;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import ss.snowiersnow.SnowierSnow;
import ss.snowiersnow.blockentity.SnowContentBlockEntity;
import ss.snowiersnow.utils.SnowHelper;

public class ModBlocks {
    public static final Tag<Block> SNOW_TAG = TagFactory.BLOCK.create(new Identifier("snowier-snow", "snow"));
    public final static Identifier SNOW_BLOCK_ID = new Identifier(SnowierSnow.MODID, "snow");
    public static DefaultSnowBlock SNOW;
    public static BlockEntityType<SnowContentBlockEntity> SNOW_BLOCK_ENTITY;
    static {
        SNOW = new DefaultSnowBlock(FabricBlockSettings.copy(Blocks.SNOW));
        SNOW_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(SnowContentBlockEntity::new, SNOW).build(null);
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
        SnowHelper.addSnowloggableBlockTag(BlockTags.FLOWERS);
        SnowHelper.addSnowloggableBlockTag(BlockTags.SAPLINGS);
//        SnowHelper.addSnowloggableBlockTag(BlockTags.FENCE_GATES);
//        SnowHelper.addSnowloggableBlockTag(BlockTags.FENCES);

        Registry.register(Registry.BLOCK, SNOW_BLOCK_ID, SNOW);
        Registry.register(Registry.BLOCK_ENTITY_TYPE, SNOW_BLOCK_ID, SNOW_BLOCK_ENTITY);
        Registry.register(Registry.ITEM, SNOW_BLOCK_ID, new BlockItem(SNOW, new FabricItemSettings().group(ItemGroup.DECORATIONS)));
    }
}
