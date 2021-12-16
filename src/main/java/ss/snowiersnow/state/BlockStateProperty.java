package ss.snowiersnow.state;

import net.minecraft.block.BlockState;
import net.minecraft.state.property.Property;

import java.util.Collection;
import java.util.Optional;

public class BlockStateProperty extends Property<ComperableBlockState> {


    protected BlockStateProperty(String name, Class<ComperableBlockState> type) {
        super(name, type);
    }

    @Override
    public Collection<ComperableBlockState> getValues() {
        return null;
    }

    @Override
    public String name(ComperableBlockState value) {
        return null;
    }

    @Override
    public Optional<ComperableBlockState> parse(String name) {
        return Optional.empty();
    }

}
