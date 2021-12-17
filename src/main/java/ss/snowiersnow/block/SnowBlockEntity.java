package ss.snowiersnow.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.Clearable;
import net.minecraft.util.math.BlockPos;
import ss.snowiersnow.SnowierSnow;

public class SnowBlockEntity extends BlockEntity implements Clearable {
    private static final String contentTagName = "rawStateId";
    private BlockState content = Blocks.AIR.getDefaultState();

    public SnowBlockEntity(BlockPos pos, BlockState state) {
        super(SnowierSnow.SNOW_BE, pos, state);
    }
    public SnowBlockEntity(BlockPos pos, BlockState state, BlockState content) {
        super(SnowierSnow.SNOW_BE, pos, state);
        this.content = content;
    }

    public void setContent(BlockState state) {
        this.content = state;
        this.updateListeners();
    }

    public static void setContent(SnowBlockEntity entity) {
        entity.content = getAir();
    }

    public BlockState getContent() {
        return content;
    }

    @Override
    public void writeNbt(NbtCompound tag) {
        super.writeNbt(tag);
        tag.putInt(contentTagName, getIdOfState(content));
    }

    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);
        content = getStateOfId(tag.getInt(contentTagName));
    }

    private int getIdOfState(BlockState state) {
        return Block.getRawIdFromState(state);
    }

    private BlockState getStateOfId(int id) {
        return Block.getStateFromRawId(id);
    }

    private static BlockState getAir() {
        return Blocks.AIR.getDefaultState();
    }

    @Override
    public String toString(){
        return "SnowBlockEnity content: " + this.content.getBlock();
    }

    public BlockEntityUpdateS2CPacket toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    public NbtCompound toInitialChunkDataNbt() {
        NbtCompound tag = new NbtCompound();
        tag.putInt(contentTagName, getIdOfState(content));
        return tag;
    }

    public boolean isEmpty(){
        return this.content != null && this.content.isAir();
    }

    @Override
    public void clear() {
        this.content = getAir();
    }

    private void updateListeners() {
        this.markDirty();
        this.getWorld().updateListeners(this.getPos(), this.getCachedState(), this.getCachedState(), Block.NOTIFY_ALL);
    }
}
