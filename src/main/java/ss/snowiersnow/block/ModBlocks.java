package ss.snowiersnow.block;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Material;
import net.minecraft.block.SnowBlock;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import ss.snowiersnow.SnowierSnow;
import ss.snowiersnow.blockentity.SnowContentBlockEntity;

public class ModBlocks {
    public final static Identifier SNOW_BLOCK_ID = new Identifier(SnowierSnow.MODID, "snow");
    public static DefaultSnowBlock SNOW_BLOCK;
    public static BlockEntityType<SnowContentBlockEntity> SNOW_BLOCK_ENTITY;

    static {
        SNOW_BLOCK = new DefaultSnowBlock(FabricBlockSettings.of(Material.SNOW_LAYER).ticksRandomly().strength(0.1F).requiresTool().sounds(BlockSoundGroup.SNOW).blockVision((state, world, pos) -> state.get(SnowBlock.LAYERS) >= 8));
        SNOW_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(SnowContentBlockEntity::new, SNOW_BLOCK).build(null);
    }

    public static void register() {
        Registry.register(Registry.BLOCK, SNOW_BLOCK_ID, SNOW_BLOCK);
        Registry.register(Registry.BLOCK_ENTITY_TYPE, SNOW_BLOCK_ID, SNOW_BLOCK_ENTITY);
        Registry.register(Registry.ITEM, SNOW_BLOCK_ID, new BlockItem(SNOW_BLOCK, new FabricItemSettings().group(ItemGroup.MISC)));
    }
}
