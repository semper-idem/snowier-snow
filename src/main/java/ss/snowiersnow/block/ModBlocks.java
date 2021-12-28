package ss.snowiersnow.block;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import ss.snowiersnow.SnowierSnow;
import ss.snowiersnow.blockentity.SnowContentBlockEntity;
import ss.snowiersnow.utils.SnowHelper;

public class ModBlocks {
    public final static Identifier SNOW_BLOCK_ID = new Identifier(SnowierSnow.MODID, "snow");
    public static DefaultSnowBlock SNOW_BLOCK;
    public static BlockEntityType<SnowContentBlockEntity> SNOW_BLOCK_ENTITY;
    static {
        SNOW_BLOCK = new DefaultSnowBlock(FabricBlockSettings.of(Material.SNOW_LAYER).ticksRandomly().strength(0.1F).requiresTool().sounds(BlockSoundGroup.SNOW).blockVision((state, world, pos) -> state.get(SnowBlock.LAYERS) >= 8));
        SNOW_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(SnowContentBlockEntity::new, SNOW_BLOCK).build(null);
    }

    public static void register() {

        SnowHelper.addBlock(Blocks.BAMBOO);
        SnowHelper.addBlock(Blocks.SUGAR_CANE);
        SnowHelper.addBlock(Blocks.SWEET_BERRY_BUSH);
        SnowHelper.addBlock(Blocks.GRASS);
        SnowHelper.addBlock(Blocks.FERN);
        SnowHelper.addBlock(Blocks.TALL_GRASS);
        SnowHelper.addBlock(Blocks.LARGE_FERN);
        SnowHelper.addBlock(Blocks.SWEET_BERRY_BUSH);
        Registry.BLOCK.forEach( block -> {
            if (block instanceof FlowerBlock) {
                SnowHelper.addBlock(block);
            }
        });
        Registry.BLOCK.forEach( block -> {
            if (block instanceof SaplingBlock) {
                SnowHelper.addBlock(block);
            }
        });
        Registry.BLOCK.forEach( block -> {
            if (block instanceof FenceBlock) {
                SnowHelper.addBlock(block);
            }
        });
        Registry.BLOCK.forEach( block -> {
            if (block instanceof TallFlowerBlock) {
                SnowHelper.addBlock(block);
            }
        });

        Registry.register(Registry.BLOCK, SNOW_BLOCK_ID, SNOW_BLOCK);
        Registry.register(Registry.BLOCK_ENTITY_TYPE, SNOW_BLOCK_ID, SNOW_BLOCK_ENTITY);
        Registry.register(Registry.ITEM, SNOW_BLOCK_ID, new BlockItem(SNOW_BLOCK, new FabricItemSettings().group(ItemGroup.MISC)));
    }
}
