package ss.snowiersnow.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FenceBlock;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;

public class FenceSnowBlock extends FenceBlock implements SnowWithContent {
    public static final IntProperty LAYERS = IntProperty.of("layers", 1, 8);

    public FenceSnowBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState()
            .with(NORTH, false)
            .with(EAST, false)
            .with(SOUTH, false)
            .with(WEST, false)
            .with(WATERLOGGED, false)
            .with(LAYERS, 1));
    }

    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, WEST, SOUTH, WATERLOGGED, LAYERS);
    }
}
