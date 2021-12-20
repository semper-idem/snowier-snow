package ss.snowiersnow.block;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ShovelItem;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.*;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.listener.GameEventListener;
import ss.snowiersnow.block.helper.Snowloggable;
import ss.snowiersnow.initializers.SnowierSnow;

import java.util.Map;
import java.util.Optional;
import java.util.Random;

public interface ISnowierBlock extends BlockEntityProvider {
    IntProperty LAYERS = Properties.LAYERS;
    VoxelShape[] LAYERS_TO_SHAPE = new VoxelShape[]{VoxelShapes.empty(), Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D), Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 4.0D, 16.0D), Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 6.0D, 16.0D), Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 8.0D, 16.0D), Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 10.0D, 16.0D), Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 12.0D, 16.0D), Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 14.0D, 16.0D), Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D)};

    default BlockState getContentState(BlockView world, BlockState state, BlockPos pos) {
        if (state.hasBlockEntity()) {
            Optional<SnowierBlockEntity> blockEntity = world.getBlockEntity(pos, SnowierSnow.SNOW_BE);
            if (blockEntity.isPresent()) {
                return blockEntity.get().getContentState();
            }
        }
        return Blocks.AIR.getDefaultState();
    }

    default ItemStack getContentItemStack(World world, BlockState state, BlockPos pos) {
        return new ItemStack(getContentState(world, state, pos).getBlock().asItem());
    }

    //todo
    default BlockSoundGroup getSoundGroup(){
        return null;
    }

    default VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        VoxelShape shape = Blocks.SNOW.getCollisionShape(state, world,pos, context);
        if (world.getBlockState(pos.up()).getBlock() instanceof ISnowierBlock) {
            int upperSnowLayers = world.getBlockState(pos.up()).get(LAYERS);
            if (upperSnowLayers == 1) {
                shape = LAYERS_TO_SHAPE[state.get(LAYERS) - 1];
            } else if (upperSnowLayers == 2) {
                return VoxelShapes.fullCube(); //this is weird
            }
        }
        shape = VoxelShapes.combine(
            shape,
            getContentState(world, state, pos).getCollisionShape(world, pos),
            BooleanBiFunction.OR
        );
        return VoxelShapes.combineAndSimplify(
            shape,
            VoxelShapes.fullCube(),
            BooleanBiFunction.AND
        );
    }

    default VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        if (state.get(LAYERS) == 8) {
            return VoxelShapes.fullCube();
        }
        return VoxelShapes.combineAndSimplify(
            Blocks.SNOW.getOutlineShape(state, world,pos, context),
            getContentState(world, state, pos).getOutlineShape(world, pos),
            BooleanBiFunction.OR
        );
    }

    default VoxelShape getSidesShape(BlockState state, BlockView world, BlockPos pos) {
        if (state.get(LAYERS) == 8) {
            return VoxelShapes.fullCube();
        }
        return VoxelShapes.combineAndSimplify(
            Blocks.SNOW.getSidesShape(state, world,pos),
            getContentState(world, state, pos).getSidesShape(world, pos),
            BooleanBiFunction.OR
        );
    }


    default boolean addSnowLayer(WorldAccess world, BlockState state, BlockPos pos, boolean accumulate, boolean shouldPlaySound) {
        if (state.getBlock() instanceof ISnowierBlock && accumulate) {
            int layers = state.get(LAYERS);
            if (layers < 8) {
                world.setBlockState(pos, state.with(LAYERS, layers + 1), Block.NOTIFY_LISTENERS);
                playSound(world, pos, shouldPlaySound);
                return true;
            } else {
                return addSnowLayer(world, world.getBlockState(pos.up()), pos.up(), false, shouldPlaySound);
            }
        } else if (state.isAir() || Snowloggable.canContain(state)) {
            world.setBlockState(pos, SnowierSnow.SNOW_BLOCK.getDefaultState(), Block.NOTIFY_LISTENERS);
            if (!state.isAir()) {
                BlockEntity blockEntity = world.getBlockEntity(pos);
                if (blockEntity instanceof SnowierBlockEntity) {
                    ((SnowierBlockEntity) blockEntity).setContentState(state);
                }
            }
            playSound(world, pos, shouldPlaySound);
            return true;
        }
        return false;
    }


    default void playSound(WorldAccess worldAccess, BlockPos pos, boolean playSound) {
        if (playSound) {
            worldAccess.playSound(null, pos, BlockSoundGroup.SNOW.getPlaceSound(), SoundCategory.BLOCKS, BlockSoundGroup.SNOW.volume, BlockSoundGroup.SNOW.pitch);
        }
    }

    default void removeSnowLayer(World world, BlockState state, BlockPos pos) {
        int layers = state.get(SnowBlock.LAYERS);
        if (layers == 1) {
            BlockState content = getContentState(world,state,pos);
            if (content.isAir()) {
                world.removeBlock(pos, false);
            } else {
                world.setBlockState(pos, content);
            }
        } else {
            world.setBlockState(pos, state.with(SnowBlock.LAYERS, layers - 1));
        }
    }

    default void melt(BlockState state, World world, BlockPos pos) {
        if (world.getLightLevel(LightType.BLOCK, pos) > 11) {
            removeSnowLayer(world, state, pos);
        }
    }

    default ActionResult onShovel(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand) {
        ItemStack stackInHand = player.getStackInHand(hand);
        if (stackInHand.getItem() instanceof ShovelItem) {
            Map<Enchantment, Integer> enchantmentsMap = EnchantmentHelper.fromNbt(stackInHand.getEnchantments());
            removeSnowLayer(world, state, pos);
            dropSnow(player, enchantmentsMap.containsKey(Enchantments.SILK_TOUCH));
            damageShovel(stackInHand, enchantmentsMap.getOrDefault(Enchantments.UNBREAKING, 0));
            world.emitGameEvent(player, GameEvent.BLOCK_CHANGE, pos);
            return ActionResult.success(world.isClient);
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
        ItemStack snow = hasSilkTouch ? new ItemStack(SnowierSnow.SNOW_BLOCK) : new ItemStack(Items.SNOWBALL);
        if (!player.giveItemStack(snow)) {
            player.dropItem(snow, false);
        }
    }

    default void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        melt(state, world, pos);
    }

    default float calcBlockBreakingDelta(BlockState state, PlayerEntity player, BlockView world, BlockPos pos) {
        float lowerHardness = state.getHardness(world, pos);
        BlockState easierToMineState = state;
        BlockState content = getContentState(world, state, pos);
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
        if (state.getBlock() == SnowierSnow.SNOW_BLOCK) {
            if (state.get(ISnowierBlock.LAYERS) <= 2) {
                if((result = getContentState(world, state, pos).onUse(world, player, hand, hit)) != ActionResult.PASS) {
                    System.out.println("here");
                    world.emitGameEvent(player, GameEvent.BLOCK_CHANGE, pos);
                    return result;
                }
            }
            if((result = onShovel(state, world, pos, player, hand)) != ActionResult.PASS) {
                System.out.println("here2");
                return result;
            }
        }
        return ActionResult.PASS;
    }



    default void onLandedUpon(World world, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        int combinedLayers = state.get(ISnowierBlock.LAYERS);
        if (world.getBlockState(pos.down()) instanceof ISnowierBlock) {
            combinedLayers += 8; //Assuming snow block below is always full which doesnt have to be the case but...
        }
        double contentHeight = 0;
        VoxelShape contentShape = getContentState(world, state, pos).getCollisionShape(world, pos);
        if (!contentShape.isEmpty()) {
            contentHeight = contentShape.getBoundingBox().maxY * 8;
        }
        float damageMultiplier = Math.max(1, Math.min(0 , 1 - (float) ((combinedLayers - contentHeight) * 0.125f)));
        entity.handleFallDamage(fallDistance, damageMultiplier, DamageSource.FALL);
    }

    default BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new SnowierBlockEntity(pos, state);
    }

    default <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return BlockEntityProvider.super.getTicker(world, state, type);
    }

    default <T extends BlockEntity> GameEventListener getGameEventListener(World world, T blockEntity) {
        return BlockEntityProvider.super.getGameEventListener(world, blockEntity);
    }

}
