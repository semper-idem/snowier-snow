package ss.snowiersnow.state;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.Property;
import net.minecraft.util.StringIdentifiable;
import org.jetbrains.annotations.NotNull;

public class ComperableBlockState extends BlockState implements StringIdentifiable, Comparable<ComperableBlockState> {
    protected ComperableBlockState(Block block, ImmutableMap<Property<?>, Comparable<?>> immutableMap, MapCodec<BlockState> mapCodec) {
        super(block, immutableMap, mapCodec);
    }

    @Override
    public String asString() {
        return null;
    }

    @Override
    public int compareTo(@NotNull ComperableBlockState o) {
        return 0;
    }


}
