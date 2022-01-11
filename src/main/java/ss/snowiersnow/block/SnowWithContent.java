package ss.snowiersnow.block;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ShovelItem;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.*;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.listener.GameEventListener;
import ss.snowiersnow.blockentity.SnowContentBlockEntity;
import ss.snowiersnow.utils.SnowHelper;

import java.util.Map;
import java.util.Random;


import static ss.snowiersnow.block.ModBlocks.SNOW_TAG;

public interface SnowWithContent extends BlockEntityProvider {
    IntProperty LAYERS = Properties.LAYERS;
    VoxelShape[] LAYERS_TO_SHAPE = new VoxelShape[]{VoxelShapes.empty(), Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D), Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 4.0D, 16.0D), Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 6.0D, 16.0D), Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 8.0D, 16.0D), Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 10.0D, 16.0D), Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 12.0D, 16.0D), Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 14.0D, 16.0D), Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D)};

    default VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        VoxelShape shape = Blocks.SNOW.getCollisionShape(state, world,pos, context);
        if (world.getBlockState(pos.up()).getBlock().getDefaultState().isIn(SNOW_TAG) ) {
            int upperSnowLayers = world.getBlockState(pos.up()).get(LAYERS);
            if (upperSnowLayers == 1) {
                shape = LAYERS_TO_SHAPE[state.get(LAYERS) - 1];
            }
            else if (upperSnowLayers == 2) {
                return VoxelShapes.fullCube(); //this is weird
            }
        }
        shape = VoxelShapes.combine(shape, SnowHelper.getContentState(world, pos).getCollisionShape(world, pos), BooleanBiFunction.OR);
        return VoxelShapes.combineAndSimplify(shape, VoxelShapes.fullCube(), BooleanBiFunction.AND);
    }

    default VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        int layers = state.get(LAYERS);
        return layers == 8 ? VoxelShapes.fullCube() : VoxelShapes.combineAndSimplify(LAYERS_TO_SHAPE[layers], SnowHelper.getContentState(world, pos).getOutlineShape(world, pos), BooleanBiFunction.OR);
    }

    default VoxelShape getSidesShape(BlockState state, BlockView world, BlockPos pos) {
        int layers = state.get(LAYERS);
        return layers == 8 ? VoxelShapes.fullCube() : VoxelShapes.combineAndSimplify(LAYERS_TO_SHAPE[layers], SnowHelper.getContentState(world, pos).getOutlineShape(world, pos), BooleanBiFunction.OR);
    }

    default boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        BlockState surface = world.getBlockState(pos.down());
        if (!surface.isOf(Blocks.ICE) && !surface.isOf(Blocks.PACKED_ICE) && !surface.isOf(Blocks.BARRIER)) {
            if (!surface.isOf(Blocks.HONEY_BLOCK) && !surface.isOf(Blocks.SOUL_SAND)) {
                return Block.isFaceFullSquare(surface.getCollisionShape(world, pos.down()), Direction.UP)
                    || surface.isIn(SNOW_TAG)
                    && surface.get(SnowWithContent.LAYERS) == 8;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    default boolean canReplace(BlockState state, ItemPlacementContext context) {
        if (!SnowHelper.getContentState(context.getWorld(), context.getBlockPos()).isAir()){
            return false;
        }
        Block blockToReplaceWith = Block.getBlockFromItem(context.getStack().getItem());
        int layers = state.get(SnowWithContent.LAYERS);
        if (blockToReplaceWith.getDefaultState().isIn(SNOW_TAG) && layers < 8) {
            if (context.canReplaceExisting()) {
                return context.getSide() == Direction.UP;
            } else {
                return true;
            }
        }

        BlockState possibleContent = SnowHelper.getContentState(context.getWorld(), context.getBlockPos());
        if (possibleContent.getBlock().asItem() == context.getStack().getItem()) {
            return possibleContent.getBlock().canReplace(possibleContent, context);
        }

        return layers == 1;
    }

    default void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (newState.isAir()) {
            BlockState content = SnowHelper.getContentState(world, pos);
            if (!content.isAir()) {
                if (state.isIn(SNOW_TAG)) {
                    boolean contentShouldBreak = SnowHelper.contentShouldBreak(state.get(LAYERS), content);
                    if (contentShouldBreak) {
                        if (SnowHelper.isContentBase(content)) {
                            world.setBlockState(pos.up(), Blocks.AIR.getDefaultState());
                        }
                        SnowHelper.setContentState(Blocks.AIR.getDefaultState(), world, pos);
                        ItemScatterer.spawn(world, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(content.getBlock()));
                        world.setBlockState(pos, state);
                    } else {
                        world.setBlockState(pos, content);
                        content.getBlock().onPlaced(world, pos, content, null ,null);
                    }
                }
            }
        } else if (SnowHelper.canContain(newState)) {
            world.setBlockState(pos, state);
            SnowHelper.setContentState(newState, world, pos);
        }
    }

    default BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        updateContent(SnowHelper.getContentState(world, pos), world, pos);
        return !state.canPlaceAt(world, pos) ? Blocks.AIR.getDefaultState() : state;
    }


    default void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (world.getLightLevel(LightType.BLOCK, pos) > 11) {
            SnowHelper.removeOrReduce(state, world, pos);
        }
        BlockState content = SnowHelper.getContentState(world, pos);
        updateContent(content, world, pos);

        if (content.hasRandomTicks()) {
            content.randomTick(world, pos, random);
        }
    }

    default void updateContent(BlockState content, WorldAccess world, BlockPos pos){
        if (!content.canPlaceAt(world, pos)) {
            SnowHelper.setContentState(Blocks.AIR.getDefaultState(), world, pos);
            if (world instanceof ServerWorld) {
                ItemScatterer.spawn((World) world, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(content.getBlock()));
            }
        }
    }

    default float calcBlockBreakingDelta(BlockState state, PlayerEntity player, BlockView world, BlockPos pos) {
        float lowerHardness = state.getHardness(world, pos);
        BlockState easierToMineState = state;
        BlockState content = SnowHelper.getContentState(world, pos);
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

    default ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ActionResult result;
        ItemStack stackInHand = player.getStackInHand(hand);
        if (stackInHand.getItem() instanceof ShovelItem) {
            Map<Enchantment, Integer> enchantmentsMap = EnchantmentHelper.fromNbt(stackInHand.getEnchantments());
            SnowHelper.removeOrReduce(state, world, pos);
            dropSnow(player, enchantmentsMap.containsKey(Enchantments.SILK_TOUCH));
            damageShovel(stackInHand, enchantmentsMap.getOrDefault(Enchantments.UNBREAKING, 0));
            world.emitGameEvent(player, GameEvent.BLOCK_CHANGE, pos);
            SnowHelper.playSound(world, pos, BlockSoundGroup.SNOW.getBreakSound());
            return ActionResult.success(world.isClient);
        }
        if (Block.getBlockFromItem(stackInHand.getItem()).getDefaultState().isIn(SNOW_TAG)) {
            if (state.get(LAYERS) != 8) {
                SnowHelper.stackSnow(state, world, pos);
                if (!player.isCreative()) {
                    stackInHand.decrement(1);
                }
                SnowHelper.playSound(world, pos, BlockSoundGroup.SNOW.getPlaceSound());
                return ActionResult.success(world.isClient);
            }
        }
        if (SnowHelper.getContentState(world, pos).isAir()) {
            Block possibleBlockContent = Block.getBlockFromItem(stackInHand.getItem());
            if (SnowHelper.canContain(possibleBlockContent)) {
                BlockState possibleBlockStateContent = possibleBlockContent.getDefaultState();
                if (possibleBlockStateContent.canPlaceAt(world, pos)) {
                    SnowHelper.putInSnow(possibleBlockStateContent, world, pos, state.get(LAYERS));
                    if (!player.isCreative()) {
                        stackInHand.decrement(1);
                    }
                    return ActionResult.success(world.isClient);
                }
            }
        }
        if (state.get(SnowWithContent.LAYERS) < 4) {
            if ((result = SnowHelper.getContentState(world, pos).onUse(world, player, hand, hit)) != ActionResult.PASS) {
                return result;
            }
        }
        return ActionResult.PASS;
    }

    default void damageShovel(ItemStack stackInHand, int unbreakingLevel) {
        if (unbreakingLevel > 0) {
            if (new Random().nextInt(1 + unbreakingLevel) > 0) {
                stackInHand.setDamage(stackInHand.getDamage() + 1);
            }
        } else {
            stackInHand.setDamage(stackInHand.getDamage() + 1);
        }
    }

    default void dropSnow(PlayerEntity player, boolean hasSilkTouch) {
        ItemStack snow = hasSilkTouch ? new ItemStack(ModBlocks.DEFAULT_SNOW) : new ItemStack(Items.SNOWBALL);
        if (!player.giveItemStack(snow)) {
            player.dropItem(snow, false);
        }
    }

    default void onLandedUpon(World world, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        int combinedLayers = state.get(SnowWithContent.LAYERS);
        if (world.getBlockState(pos.down()).isIn(SNOW_TAG)) {
            combinedLayers += 8;
        }
        double contentHeight = 0;
        VoxelShape contentShape = SnowHelper.getContentState(world, pos).getCollisionShape(world, pos);
        if (!contentShape.isEmpty()) {
            contentHeight = contentShape.getBoundingBox().maxY * 8;
        }
        float damageMultiplier = Math.max(1, Math.min(0 , 1 - (float) ((combinedLayers - contentHeight) * 0.125f)));
        entity.handleFallDamage(fallDistance, damageMultiplier, DamageSource.FALL);
    }

    default void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        BlockState content = SnowHelper.getContentState(world, pos);
        if (content.isOf(Blocks.SWEET_BERRY_BUSH)){
            onBerryBushEntityCollision(content, world, pos, entity);
        }
    }

    default void onBerryBushEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (entity instanceof LivingEntity && entity.getType() != EntityType.FOX && entity.getType() != EntityType.BEE) {
            entity.slowMovement(state, new Vec3d(0.8, 0.7D, 0.8));
            if (!world.isClient && state.get(SweetBerryBushBlock.AGE) > 0 && (entity.lastRenderX != entity.getX() || entity.lastRenderZ != entity.getZ())) {
                double d = Math.abs(entity.getX() - entity.lastRenderX);
                double e = Math.abs(entity.getZ() - entity.lastRenderZ);
                if (d >= 0.003 || e >= 0.003) {
                    entity.damage(DamageSource.SWEET_BERRY_BUSH, 1.0F);
                }
            }
        }
    }

    default BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new SnowContentBlockEntity(pos, state);
    }

    default <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return BlockEntityProvider.super.getTicker(world, state, type);
    }

    default <T extends BlockEntity> GameEventListener getGameEventListener(World world, T blockEntity) {
        return BlockEntityProvider.super.getGameEventListener(world, blockEntity);
    }
}