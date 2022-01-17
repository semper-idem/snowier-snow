package ss.snowiersnow.registry;

import net.fabricmc.fabric.api.tag.TagFactory;
import net.minecraft.block.Block;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;

public class ModTags {
    public static final Tag<Block> SNOW_BLOCK_TAG;
    public static final Tag<Block> SNOWLOGGABLE_TAG;
    public static final Tag<Block> TICK_ALLOWED_TAG;

    static {
        SNOW_BLOCK_TAG = TagFactory.BLOCK.create(new Identifier("snowier-snow", "snow_block"));
        SNOWLOGGABLE_TAG = TagFactory.BLOCK.create(new Identifier("snowier-snow", "snowloggable"));
        TICK_ALLOWED_TAG = TagFactory.BLOCK.create(new Identifier("snowier-snow", "tick_allowed"));
    }
}
