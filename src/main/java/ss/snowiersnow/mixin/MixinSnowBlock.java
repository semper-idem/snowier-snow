package ss.snowiersnow.mixin;


import net.minecraft.block.*;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ShovelItem;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.event.GameEvent;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ss.snowiersnow.state.SnowBlockState;
import static ss.snowiersnow.state.SnowBlockState.CONTENT;
import static ss.snowiersnow.state.SnowBlockState.getContent;

import java.util.Map;
import java.util.Random;

@Mixin(SnowBlock.class)
public class MixinSnowBlock extends Block{

    @Shadow @Final protected static VoxelShape[] LAYERS_TO_SHAPE;
    @Shadow @Final public static IntProperty LAYERS;

    public MixinSnowBlock(Settings settings) {
        super(settings);
    }

    @Inject(at = @At("TAIL"), method = "<init>")
    private void onSnowBlock(AbstractBlock.Settings settings, CallbackInfo ci){
            this.setDefaultState(this.stateManager.getDefaultState()
                .with(LAYERS, 1)
                .with(CONTENT, SnowBlockState.AIR));
    }

    @Inject(at = @At("HEAD"), method = "appendProperties")
    private void onAppendProperties(StateManager.Builder<Block, BlockState> builder, CallbackInfo ci){
        builder.add(CONTENT);
    }

    @Inject(at = @At("HEAD"), method = "getOutlineShape", cancellable = true)
    public void getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context, CallbackInfoReturnable<VoxelShape> cir) {
        if (!isEmpty(state)) {
            cir.setReturnValue(VoxelShapes.union(
                LAYERS_TO_SHAPE[state.get(LAYERS)],
                getContent(state).getOutlineShape(world, pos)));
            cir.cancel();
        }
    }

    @Inject(at = @At("HEAD"), method = "getCollisionShape", cancellable = true)
    public void onGetCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context, CallbackInfoReturnable<VoxelShape> cir) {
        int layers = state.get(SnowBlock.LAYERS);
        VoxelShape collisionShape = LAYERS_TO_SHAPE[layers <= 2 ? 0 : layers - 2];
        if (!isEmpty(state)) {
            collisionShape = VoxelShapes.union(
                collisionShape,
                getContent(state).getCollisionShape(world,pos)
            );
        }
        cir.setReturnValue(collisionShape);
        cir.cancel();
    }


    /**
     * @author snowier-snow akio
     */
    @Overwrite
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        int layers = state.get(LAYERS);
        if (world.getLightLevel(LightType.BLOCK, pos) > 11) {
            if (layers == 1) {
                if (isEmpty(state)) {
                    dropStacks(state, world, pos);
                    world.removeBlock(pos, false);
                } else {
                    world.setBlockState(pos, getContent(state));
                }
            } else {
                world.setBlockState(pos, state.with(SnowBlock.LAYERS, layers - 1));
            }
        }
    }

    @Override
    public void onBroken(WorldAccess world, BlockPos pos, BlockState state) {
        if (!isEmpty(state)) {
            BlockState blockInside = getContent(state);
            if (blockInside.getHardness(world, pos) < state.getHardness(world, pos)) {
                blockInside.getBlock().onBroken(world,pos,state);
                world.setBlockState(pos, state.with(SnowBlockState.CONTENT, SnowBlockState.AIR), 3);
            } else {
                world.setBlockState(pos, blockInside, 3);
            }
        }
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ActionResult result;
        BlockState content = state.get(SnowBlockState.CONTENT).getBlockState();

        if (state.get(LAYERS) < 4) {
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

    private boolean isEmpty(BlockState state) {
        return (state.get(CONTENT) == SnowBlockState.AIR);
    }

    @Override
    public float calcBlockBreakingDelta(BlockState state, PlayerEntity player, BlockView world, BlockPos pos) {
        float lowerHardness = state.getHardness(world, pos);
        BlockState easierToMineState = state;
        if (!isEmpty(state)) {
            BlockState content = getContent(state);
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
}
