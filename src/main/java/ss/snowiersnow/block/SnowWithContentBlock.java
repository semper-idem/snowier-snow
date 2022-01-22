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
import net.minecraft.item.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
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
import ss.snowiersnow.blockentity.ContentBlockEntity;
import ss.snowiersnow.registry.ModTags;
import ss.snowiersnow.utils.SnowHelper;
import ss.snowiersnow.utils.Snowloggable;

import java.util.Map;
import java.util.Random;


@SuppressWarnings("deprecation")
public class SnowWithContentBlock extends SnowBlock implements BlockEntityProvider  {
    public SnowWithContentBlock(Settings settings) {
        super(settings);
    }

    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        VoxelShape shape = Blocks.SNOW.getCollisionShape(state, world,pos, context);
        if (world.getBlockState(pos.up()).getBlock().getDefaultState().isIn(ModTags.SNOW_BLOCK_TAG) ) {
            int upperSnowLayers = world.getBlockState(pos.up()).get(LAYERS);
            if (upperSnowLayers == 1) {
                shape = LAYERS_TO_SHAPE[state.get(LAYERS) - 1];
            }
            else if (upperSnowLayers == 2) {
                return VoxelShapes.fullCube(); //this is weird
            }
        }
        return VoxelShapes.combineAndSimplify(
            VoxelShapes.combine(
                shape,
                ContentBlockEntity.getContent(world, pos).getCollisionShape(world, pos),
                BooleanBiFunction.OR
            ),
            VoxelShapes.fullCube()
            , BooleanBiFunction.AND
        );
    }

    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        int layers = state.get(LAYERS);
        return layers == 8 ?
            VoxelShapes.fullCube() :
            VoxelShapes.combineAndSimplify(
                LAYERS_TO_SHAPE[layers],
                ContentBlockEntity.getContent(world, pos).getOutlineShape(world, pos),
                BooleanBiFunction.OR
            );
    }

    public VoxelShape getSidesShape(BlockState state, BlockView world, BlockPos pos) {
        int layers = state.get(LAYERS);
        return layers == 8 ?
            VoxelShapes.fullCube() :
            VoxelShapes.combineAndSimplify(
                LAYERS_TO_SHAPE[layers],
                ContentBlockEntity.getContent(world, pos).getOutlineShape(world, pos),
                BooleanBiFunction.OR
            );
    }

    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        BlockState surface = world.getBlockState(pos.down());
        if (!surface.isOf(Blocks.ICE) && !surface.isOf(Blocks.PACKED_ICE) && !surface.isOf(Blocks.BARRIER)) {
            if (!surface.isOf(Blocks.HONEY_BLOCK) && !surface.isOf(Blocks.SOUL_SAND)) {
                return Block.isFaceFullSquare(surface.getCollisionShape(world, pos.down()), Direction.UP)
                    || surface.isIn(ModTags.SNOW_BLOCK_TAG)
                    && surface.get(LAYERS) == 8;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    public boolean canReplace(BlockState state, ItemPlacementContext context) {
        return false;
    }

    public void onBroken(WorldAccess world, BlockPos pos, BlockState state) {
        BlockState content = ContentBlockEntity.getContent(world, pos);
        ((World)world).removeBlockEntity(pos);
        int layers = state.get(LAYERS);
        if (shouldContentBreak(world, pos, (layers == 8), content)) { //TODO Make configurable
            world.setBlockState(pos, Blocks.SNOW.getDefaultState().with(LAYERS, layers), Block.NOTIFY_NEIGHBORS);
            ItemScatterer.spawn((World)world, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(content.getBlock()));
        } else {
            world.setBlockState(pos, content, Block.NOTIFY_NEIGHBORS);
        }
    }

    private boolean shouldContentBreak(WorldAccess world, BlockPos pos, boolean isSnowFullBlock, BlockState content) {
        if (!content.isAir()) {
            float contentHardness = content.getHardness(world, pos);
            float snowHardness = isSnowFullBlock ? 0.2F : 0.1F;
            return contentHardness < snowHardness;
        }
        return false;
    }

    //Crude way to let content grow, could be improved if we knew source of this change
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (Snowloggable.canSnowContain(newState) && !moved) {
            world.setBlockState(pos, state);
            ContentBlockEntity.setContent(newState, world, pos);
        }
    }

    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        BlockState content = ContentBlockEntity.getContent(world, pos);
        if (!content.canPlaceAt(world ,pos)) {
            int layers = state.get(LAYERS);
            if (world instanceof World) {
                ItemScatterer.spawn((World) world, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(content.getBlock()));
            }
            world.removeBlock(pos, false);
            world.setBlockState(pos, Blocks.SNOW.getDefaultState().with(LAYERS, layers), Block.NOTIFY_NEIGHBORS);
        } else {
                ContentBlockEntity.setContent(content.getStateForNeighborUpdate(direction, neighborState, world, pos, neighborPos), world, pos);
         }
        return !state.canPlaceAt(world, pos) ? Blocks.AIR.getDefaultState() : state;
    }


    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        BlockState content = ContentBlockEntity.getContent(world, pos);
        if (content.isAir()) {
            world.setBlockState(pos, Blocks.SNOW.getDefaultState().with(LAYERS, state.get(LAYERS)));
        } else {
            if (world.getLightLevel(LightType.BLOCK, pos) > 11) {
                SnowHelper.meltLayer(state, world, pos);
            } else {
                if (content.canPlaceAt(world, pos) && content.hasRandomTicks() ){//&& content.isIn(ModTags.TICKING_ALLOWED)) {
                    content.randomTick(world, pos, random);
                } else if (!content.canPlaceAt(world, pos)){
                    ItemScatterer.spawn(world, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(content.getBlock()));
                    int layers = state.get(LAYERS);
                    world.setBlockState(pos, Blocks.SNOW.getDefaultState().with(LAYERS, layers));
                }
            }
        }
    }
    public float calcBlockBreakingDelta(BlockState state, PlayerEntity player, BlockView world, BlockPos pos) {
        float lowerHardness = state.getHardness(world, pos);
        BlockState easierToMineState = state;
        BlockState content = ContentBlockEntity.getContent(world, pos);
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

    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ItemStack stackInHand = player.getStackInHand(hand);
        int layers = state.get(LAYERS);
        if (stackInHand.getItem() instanceof ShovelItem) {
            return onShovelUse(state, world, pos, player, stackInHand);
        } else if (stackInHand.isOf(Items.SNOW)) {
            return onSnowLayerUse(state, world, pos, player, stackInHand, layers);
        } else if (layers < 4) {
            return ContentBlockEntity.getContent(world, pos).onUse(world, player, hand, hit);
        } else {
            return ActionResult.PASS;
        }
    }

    private ActionResult onSnowLayerUse(BlockState state, World world, BlockPos pos, PlayerEntity player, ItemStack stackInHand, int layers) {
        if (layers < 8) {
            world.setBlockState(pos, state.with(LAYERS, layers + 1));
            if (!player.isCreative()) {
                stackInHand.decrement(1);
            }
            return ActionResult.success(world.isClient);
        }
        return ActionResult.PASS;
    }

    private ActionResult onShovelUse(BlockState state, World world, BlockPos pos, PlayerEntity player, ItemStack stackInHand) {
        Map<Enchantment, Integer> enchantmentsMap = EnchantmentHelper.fromNbt(stackInHand.getEnchantments());
        SnowHelper.meltLayer(state, world, pos);
        dropSnow(player, enchantmentsMap.containsKey(Enchantments.SILK_TOUCH));
        damageShovel(stackInHand, enchantmentsMap.getOrDefault(Enchantments.UNBREAKING, 0));
        world.emitGameEvent(player, GameEvent.BLOCK_CHANGE, pos);
        SnowHelper.playSound(world, pos, BlockSoundGroup.SNOW.getBreakSound());
        return ActionResult.success(world.isClient);
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
        ItemStack snow = hasSilkTouch ? new ItemStack(Blocks.SNOW) : new ItemStack(Items.SNOWBALL);
        if (!player.giveItemStack(snow)) {
            player.dropItem(snow, false);
        }
    }

    public void onLandedUpon(World world, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        int combinedLayers = state.get(LAYERS);
        if (world.getBlockState(pos.down()).isIn(ModTags.SNOW_BLOCK_TAG)) {
            combinedLayers += 8;
        }
        double contentHeight = 0;
        VoxelShape contentShape = ContentBlockEntity.getContent(world, pos).getCollisionShape(world, pos);
        if (!contentShape.isEmpty()) {
            contentHeight = contentShape.getBoundingBox().maxY * 8;
        }
        float damageMultiplier = Math.max(1, Math.min(0 , 1 - (float) ((combinedLayers - contentHeight) * 0.125f)));
        entity.handleFallDamage(fallDistance, damageMultiplier, DamageSource.FALL);
    }

    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        BlockState content = ContentBlockEntity.getContent(world, pos);
        if (content.isOf(Blocks.SWEET_BERRY_BUSH)){
            onBerryBushEntityCollision(content, world, entity);
        }
    }

    private void onBerryBushEntityCollision(BlockState state, World world, Entity entity) {
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

    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ContentBlockEntity(pos, state);
    }

    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return BlockEntityProvider.super.getTicker(world, state, type);
    }

    public <T extends BlockEntity> GameEventListener getGameEventListener(World world, T blockEntity) {
        return BlockEntityProvider.super.getGameEventListener(world, blockEntity);
    }
}
