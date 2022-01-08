package ss.snowiersnow.block;

import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.PiglinBrain;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.*;
import net.minecraft.world.event.GameEvent;
import ss.snowiersnow.utils.SnowHelper;

import java.util.Random;

@SuppressWarnings("deprecation")
public class DefaultSnowBlock extends SnowBlock implements ISnowVariant {
    public DefaultSnowBlock(Settings settings) {
        super(settings);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return ISnowVariant.super.getCollisionShape(state, world, pos, context);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return  ISnowVariant.super.getOutlineShape(state, world,pos, context);
    }

    @Override
    public VoxelShape getSidesShape(BlockState state, BlockView world, BlockPos pos) {
        return  ISnowVariant.super.getSidesShape(state, world,pos);
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        return ISnowVariant.super.canPlaceAt(state, world,pos);
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        ISnowVariant.super.randomTick(state, world, pos, random);
    }

    @Override
    public boolean canReplace(BlockState state, ItemPlacementContext context) {
        return ISnowVariant.super.canReplace(state, context);
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        ISnowVariant.super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Override
    public void onLandedUpon(World world, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        ISnowVariant.super.onLandedUpon(world, state, pos, entity, fallDistance);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        return ISnowVariant.super.onUse(state, world, pos, player, hand, hit);
    }

    @Override
    public float calcBlockBreakingDelta(BlockState state, PlayerEntity player, BlockView world, BlockPos pos) {
        return ISnowVariant.super.calcBlockBreakingDelta(state, player, world, pos);
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        ISnowVariant.super.onEntityCollision(state, world, pos, entity);
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        return ISnowVariant.super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }
}
