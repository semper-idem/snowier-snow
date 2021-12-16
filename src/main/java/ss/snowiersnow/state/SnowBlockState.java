package ss.snowiersnow.state;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.StringIdentifiable;

import java.util.Arrays;
import java.util.Collection;

public enum SnowBlockState implements StringIdentifiable {
    POPPY,
    DRAGON_EGG,
    OAK_FENCE_GATE_OPEN,
    OAK_FENCE_GATE_CLOSE,
    AIR;





    private BlockState state;

    SnowBlockState(){
        state = Blocks.AIR.getDefaultState();
    }



    public BlockState getBlockState(){
        return state;
    }

    @Override
    public String asString() {
        return this.toString().toLowerCase();
    }

    public static SnowBlockState get(BlockState state){
        for(SnowBlockState snowBlockState : SnowBlockState.values()) {
            if (state.isOf(snowBlockState.state.getBlock())) {
               if (blockStatesMatch(state, snowBlockState.state)) {
                   return snowBlockState;
               }
            }
        }
        return AIR;
    }

    public static void setBlockStates(){
        POPPY.state = Blocks.POPPY.getDefaultState();
        DRAGON_EGG.state = Blocks.DRAGON_EGG.getDefaultState();
        OAK_FENCE_GATE_OPEN.state = Blocks.OAK_FENCE_GATE.getDefaultState().with(Properties.OPEN, true);
        OAK_FENCE_GATE_CLOSE.state = Blocks.OAK_FENCE_GATE.getDefaultState().with(Properties.OPEN, false);
    }

    private static boolean blockStatesMatch(BlockState state, BlockState otherState) {
        Collection<Property<?>> props = state.getProperties();
        if (props.containsAll(otherState.getProperties())) {
            return props.stream().allMatch( property -> state.get(property) == otherState.get(property));
        }
        return false;
    }

    public static boolean contains(BlockState state) {
        return Arrays.stream(SnowBlockState.values()).anyMatch( match -> match.getBlockState() == state);
    }

    public static BlockState getContent(BlockState state) {
        return state.get(CONTENT).getBlockState();
    }
    public static boolean isAir(SnowBlockState snowBlockState){
        return snowBlockState.state.isOf(Blocks.AIR);
    }

    public static final EnumProperty<SnowBlockState> CONTENT = EnumProperty.of("content", SnowBlockState.class);
}
