package ss.snowiersnow.blockentity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.Clearable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import ss.snowiersnow.registry.ModBlocks;

public class ContentBlockEntity extends BlockEntity implements Clearable {
    public static final String contentTagName = "rawStateId";
    private BlockState content = Blocks.AIR.getDefaultState();

    public ContentBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.CONTENT_ENTITY, pos, state);
    }

    public void setContent(BlockState newContent) {
        this.content = newContent;
        this.updateListeners();
    }

    public BlockState getContent() {
        return content != null ? content : Blocks.AIR.getDefaultState();
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

    public BlockEntityUpdateS2CPacket toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    public NbtCompound toInitialChunkDataNbt() {
        NbtCompound tag = new NbtCompound();
        tag.putInt(contentTagName, getIdOfState(content));
        return tag;
    }

    @Override
    public void clear() {
        this.content = Blocks.AIR.getDefaultState();
        updateListeners();
    }

    private void updateListeners() {
        this.markDirty();
        if (this.world != null) {
            this.world.updateListeners(this.getPos(), this.getCachedState(), this.getCachedState(), Block.NOTIFY_ALL);
        }
    }

    public static ContentBlockEntity getBlockEntity(BlockView world, BlockPos pos) {
        return world.getBlockEntity(pos, ModBlocks.CONTENT_ENTITY).orElse(null);
    }

    public static void setContent(BlockState content, BlockView world , BlockPos pos) {
        getBlockEntity(world, pos).setContent(content);
    }

    public static BlockState getContent(BlockView world, BlockPos pos) {
        ContentBlockEntity blockEntity;
        return (blockEntity = getBlockEntity(world, pos)) != null ? blockEntity.getContent() : Blocks.AIR.getDefaultState();
    }
}
