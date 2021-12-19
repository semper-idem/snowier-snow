package ss.snowiersnow.block;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.CampfireBlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ShovelItem;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.*;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;
import ss.snowiersnow.block.helper.Snowloggable;
import ss.snowiersnow.initializers.SnowierSnow;

import java.util.Map;
import java.util.Optional;
import java.util.Random;

public class SnowierBlock extends SnowBlock implements BlockEntityProvider {
    public SnowierBlock(Settings settings) {
        super(settings);
    }

    public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
        if (type == NavigationType.LAND) {
            return state.get(LAYERS) < 5;
        }
        return false;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        Optional<SnowierBlockEntity> optionalContent = world.getBlockEntity(pos, SnowierSnow.SNOW_BE);
        if (optionalContent.isPresent()) {
            if (!optionalContent.get().isEmpty()){
                return VoxelShapes.union(LAYERS_TO_SHAPE[state.get(LAYERS)],
                    optionalContent.get().getContent().getOutlineShape(world, pos, context));
            }
        }
        return LAYERS_TO_SHAPE[state.get(LAYERS)];
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        VoxelShape snowShape = super.getCollisionShape(state, world,pos, context);
        Optional<SnowierBlockEntity> optionalContent = world.getBlockEntity(pos, SnowierSnow.SNOW_BE);
        if (optionalContent.isPresent()) {
            BlockState content = optionalContent.get().getContent();
            if (!content.isAir()){
                if (content.getBlock() instanceof HorizontalConnectingBlock) {
                    snowShape = VoxelShapes.combine(snowShape, content.getCollisionShape(world, pos), BooleanBiFunction.OR);
                    return VoxelShapes.combineAndSimplify(snowShape, VoxelShapes.fullCube(), BooleanBiFunction.AND);
                }
                return VoxelShapes.union(snowShape, content.getCollisionShape(world, pos));
            }
        }
        return snowShape;
    }


    @Override
    public VoxelShape getSidesShape(BlockState state, BlockView world, BlockPos pos) {
        Optional<SnowierBlockEntity> optionalContent = world.getBlockEntity(pos, SnowierSnow.SNOW_BE);
        if (optionalContent.isPresent()) {
            if (!optionalContent.get().isEmpty()){
                return VoxelShapes.union(LAYERS_TO_SHAPE[state.get(LAYERS)],
                    optionalContent.get().getContent().getSidesShape(world, pos));
            }
        }
        return LAYERS_TO_SHAPE[state.get(LAYERS)];
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        return !state.canPlaceAt(world, pos) ? Blocks.AIR.getDefaultState() : super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }


    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        BlockState blockState = world.getBlockState(pos.down());
        if (!blockState.isOf(Blocks.ICE) && !blockState.isOf(Blocks.PACKED_ICE) && !blockState.isOf(Blocks.BARRIER)) {
            if (!blockState.isOf(Blocks.HONEY_BLOCK) && !blockState.isOf(Blocks.SOUL_SAND)) {
                return Block.isFaceFullSquare(blockState.getCollisionShape(world, pos.down()), Direction.UP) || blockState.isOf(this) && (Integer)blockState.get(LAYERS) == 8;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        int layers = state.get(LAYERS);
        if (world.getLightLevel(LightType.BLOCK, pos) > 11) {
            if (layers == 1) {
                if (Snowloggable.isEmpty(world, pos)) {
                    dropStacks(state, world, pos);
                    world.removeBlock(pos, false);
                } else {
                    world.setBlockState(pos, Snowloggable.getContent(world, pos));
                }
            } else {
                world.setBlockState(pos, state.with(SnowBlock.LAYERS, layers - 1));
            }
        }
    }


    @Override
    public boolean canReplace(BlockState state, ItemPlacementContext context) {
        int i = state.get(LAYERS);
        if (context.getStack().isOf(this.asItem()) && i < 8) {
            if (context.canReplaceExisting()) {
                return context.getSide() == Direction.UP;
            } else {
                return true;
            }
        } else {
            return i == 1;
        }
    }

    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock())) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof SnowierBlockEntity) {
                BlockState content = ((SnowierBlockEntity) blockEntity).getContent();
                if (!content.isAir()) {
                    if (content.getHardness(world, pos) < state.getHardness(world, pos)) {
                        ItemStack stack = new ItemStack(content.getBlock());
                        ItemScatterer.spawn(world, pos.getX(), pos.getY(), pos.getZ(), stack);
                        ((SnowierBlockEntity) blockEntity).clear();
                        world.setBlockState(pos, state);
                    } else {
                        ItemStack stack = new ItemStack(Items.SNOWBALL, state.get(LAYERS));
                        ItemScatterer.spawn(world, pos.getX(), pos.getY(), pos.getZ(), stack);
                        world.setBlockState(pos, content);
                    }
                } else {
                    super.onStateReplaced(state, world, pos, newState, moved);
                }
            }
        }
    }
    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ActionResult result;
        BlockState content = Snowloggable.getContent(world, pos);
        if (state.get(LAYERS) <= 2) {
            if((result = content.onUse(world, player, hand, hit)) != ActionResult.PASS) {
                world.emitGameEvent(player, GameEvent.BLOCK_CHANGE, pos);
                return result;
            }
        }
        if((result = onShovel(state, content, world, pos, player, hand)) != ActionResult.PASS) {
            return result;
        }
        return ActionResult.PASS;
    }

    private ActionResult onShovel(BlockState state, BlockState content, World world, BlockPos pos, PlayerEntity player, Hand hand) {
        ItemStack stackInHand = player.getStackInHand(hand);
        if (stackInHand.getItem() instanceof ShovelItem) {
            Map<Enchantment, Integer> enchantmentsMap = EnchantmentHelper.fromNbt(stackInHand.getEnchantments());
            removeSnow(world, state, content, pos);
            dropSnow(player, enchantmentsMap.containsKey(Enchantments.SILK_TOUCH));
            damageShovel(stackInHand, enchantmentsMap.getOrDefault(Enchantments.UNBREAKING, 0));
            world.emitGameEvent(player, GameEvent.BLOCK_CHANGE, pos);
            return ActionResult.success(world.isClient);
        }
        return ActionResult.PASS;
    }

    private void damageShovel(ItemStack stackInHand, int unbreakingLevel) {
        if (unbreakingLevel > 0) {
            if (new Random().nextInt(1 + unbreakingLevel) > 0) {
                stackInHand.setDamage(stackInHand.getDamage() + 1);
            }
        } else {
            stackInHand.setDamage(stackInHand.getDamage() + 1);
        }
    }

    private void dropSnow(PlayerEntity player, boolean hasSilkTouch) {
        ItemStack snow = hasSilkTouch ? new ItemStack(Items.SNOW) : new ItemStack(Items.SNOWBALL);
        if (!player.giveItemStack(snow)) {
            player.dropItem(snow, false);
        }
    }

    private void removeSnow(World world, BlockState state, BlockState content, BlockPos pos) {
        int layers = state.get(LAYERS);
        if (layers > 1) {
            world.setBlockState(pos, state.with(LAYERS, layers - 1));
        } else {
            world.setBlockState(pos, content);
        }
    }

    @Override
    public float calcBlockBreakingDelta(BlockState state, PlayerEntity player, BlockView world, BlockPos pos) {
        float lowerHardness = state.getHardness(world, pos);
        BlockState easierToMineState = state;
        BlockState content = Snowloggable.getContent(world, pos);
        if (!content.isAir()) {
            float contentHardness = content.getHardness(world,pos);
            if (contentHardness < lowerHardness) {
                easierToMineState = content;
                lowerHardness = contentHardness;
            }
        }
        if (lowerHardness == -1.0F) {
            return 0.0F;
        } else {
            int i = player.canHarvest(easierToMineState) ? 30 : 100;
            return player.getBlockBreakingSpeed(easierToMineState) / lowerHardness / (float)i;
        }
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new SnowierBlockEntity(pos, state);
    }

}
