package org.thewitcher.facto.block.pipe;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.network.NetworkHooks;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.thewitcher.facto.Registry;
import org.thewitcher.facto.Utility;
import org.apache.commons.lang3.tuple.Pair;
import org.thewitcher.facto.item.modules.IModule;
import org.thewitcher.facto.network.PipeNetwork;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.annotation.Nonnull;

public class PipeBlock extends BaseEntityBlock {

    public static final Map<Direction, EnumProperty<Connection>> DIRECTIONS = new HashMap<>();
    public static final Map<Pair<BlockState, BlockState>, VoxelShape> SHAPE_CACHE = new HashMap<>();
    public static final Map<Pair<BlockState, BlockState>, VoxelShape> COLL_SHAPE_CACHE = new HashMap<>();
    private static final VoxelShape CENTER_SHAPE = Block.box(5, 5, 5, 11, 11, 11);
    public static final Map<Direction, VoxelShape> DIR_SHAPES = ImmutableMap.<Direction, VoxelShape>builder()
            .put(Direction.UP, Block.box(5, 10, 5, 11, 16, 11))
            .put(Direction.DOWN, Block.box(5, 0, 5, 11, 6, 11))
            .put(Direction.NORTH, Block.box(5, 5, 0, 11, 11, 6))
            .put(Direction.SOUTH, Block.box(5, 5, 10, 11, 11, 16))
            .put(Direction.EAST, Block.box(10, 5, 5, 16, 11, 11))
            .put(Direction.WEST, Block.box(0, 5, 5, 6, 11, 11))
            .build();

    static {
        for (var dir : Direction.values())
            PipeBlock.DIRECTIONS.put(dir, EnumProperty.create(dir.getName(), Connection.class));
    }

    public PipeBlock() {
        super(BlockBehaviour.Properties.of().strength(2).sound(SoundType.STONE).noOcclusion());

        var state = this.defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, false);
        for(var prop: PipeBlock.DIRECTIONS.values()) {
            state = state.setValue(prop, Connection.DISCONNECTED);
        }

        this.registerDefaultState(state);
    }

    @Override
    public @NotNull InteractionResult use(@Nonnull BlockState state, @Nonnull Level worldIn, @Nonnull BlockPos pos, @Nonnull Player player, @Nonnull InteractionHand handIn, @Nonnull BlockHitResult result) {
        var tile = Utility.getBlockEntity(PipeBlockEntity.class, worldIn, pos);
        if (tile == null)
            return InteractionResult.PASS;
        if (!tile.canHaveModules())
            return InteractionResult.PASS;
        var stack = player.getItemInHand(handIn);
        if (stack.getItem() instanceof IModule) {
            var copy = stack.copy();
            copy.setCount(1);
            var remain = ItemHandlerHelper.insertItem(tile.modules, copy, false);
            if (remain.isEmpty()) {
                stack.shrink(1);
                return InteractionResult.SUCCESS;
            }
        } else if (handIn == InteractionHand.MAIN_HAND && stack.isEmpty()) {
            if (!worldIn.isClientSide)
                NetworkHooks.openScreen((ServerPlayer) player, tile, pos);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    protected void createBlockStateDefinition(@Nonnull StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PipeBlock.DIRECTIONS.values().toArray(new EnumProperty[0]));
        builder.add(BlockStateProperties.WATERLOGGED);
    }

    @Override
    public @NotNull FluidState getFluidState(@Nonnull BlockState state) {
        return state.getValue(BlockStateProperties.WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public void neighborChanged(@Nonnull BlockState state, @Nonnull Level worldIn, @Nonnull BlockPos pos, @Nonnull Block blockIn, @Nonnull BlockPos fromPos, boolean isMoving) {
        var newState = this.createState(worldIn, pos, state);
        if(newState != state) {
            worldIn.setBlockAndUpdate(pos, newState);
            PipeBlock.onStateChanged(worldIn, pos, newState);
        }
    }

    @Override
    public void setPlacedBy(@Nonnull Level worldIn, @Nonnull BlockPos pos, @Nonnull BlockState state, @javax.annotation.Nullable LivingEntity placer, @Nonnull ItemStack stack) {
        PipeBlock.onStateChanged(worldIn, pos, state);
    }

    @Override
    public @NotNull BlockState updateShape(@Nonnull BlockState stateIn, @Nonnull Direction facing, @Nonnull BlockState facingState, @Nonnull LevelAccessor worldIn, @Nonnull BlockPos currentPos, @Nonnull BlockPos facingPos) {
        if (stateIn.getValue(BlockStateProperties.WATERLOGGED))
            worldIn.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(worldIn));
        return super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(@Nonnull BlockPlaceContext context) {
        return this.createState(context.getLevel(), context.getClickedPos(), this.defaultBlockState());
    }

    @Override
    public VoxelShape getShape(@Nonnull BlockState state, @Nonnull BlockGetter worldIn, @Nonnull BlockPos pos, @Nonnull CollisionContext context) {
        return this.cacheAndGetShape(state, worldIn, pos, s -> s.getShape(worldIn, pos, context), PipeBlock.SHAPE_CACHE, null);
    }

    @Override
    public VoxelShape getCollisionShape(@Nonnull BlockState state, @Nonnull BlockGetter worldIn, @Nonnull BlockPos pos, @Nonnull CollisionContext context) {
        return this.cacheAndGetShape(state, worldIn, pos, s -> s.getCollisionShape(worldIn, pos, context), PipeBlock.COLL_SHAPE_CACHE, s -> {
            // make the shape a bit higher so we can jump up onto a higher block
            var newShape = new MutableObject<VoxelShape>(Shapes.empty());
            s.forAllBoxes((x1, y1, z1, x2, y2, z2) -> newShape.setValue(Shapes.join(Shapes.create(x1, y1, z1, x2, y2 + 3 / 16F, z2), newShape.getValue(), BooleanOp.OR)));
            return newShape.getValue().optimize();
        });
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        return new PipeBlockEntity(pos, state);
    }

    @Override
    public @NotNull RenderShape getRenderShape(@Nonnull BlockState state) {
        return RenderShape.MODEL;
    }

    private VoxelShape cacheAndGetShape(BlockState state, BlockGetter worldIn, BlockPos pos, Function<BlockState, VoxelShape> coverShapeSelector, Map<Pair<BlockState, BlockState>, VoxelShape> cache, Function<VoxelShape, VoxelShape> shapeModifier) {
        VoxelShape coverShape = null;
        BlockState cover = null;
        var tile = Utility.getBlockEntity(PipeBlockEntity.class, worldIn, pos);
        if (tile != null && tile.cover != null) {
            cover = tile.cover;
            // try catch since the block might expect to find itself at the position
            try {
                coverShape = coverShapeSelector.apply(cover);
            } catch (Exception ignored) {
            }
        }
        var key = Pair.of(state, cover);
        var shape = cache.get(key);
        if (shape == null) {
            shape = PipeBlock.CENTER_SHAPE;
            for (var entry : PipeBlock.DIRECTIONS.entrySet()) {
                if (state.getValue(entry.getValue()).isConnected())
                    shape = Shapes.or(shape, PipeBlock.DIR_SHAPES.get(entry.getKey()));
            }
            if (shapeModifier != null)
                shape = shapeModifier.apply(shape);
            if (coverShape != null)
                shape = Shapes.or(shape, coverShape);
            cache.put(key, shape);
        }
        return shape;
    }


    private BlockState createState(Level world, BlockPos pos, BlockState curr) {
        var state = this.defaultBlockState();
        var fluid = world.getFluidState(pos);
        if (fluid.is(FluidTags.WATER) && fluid.getAmount() == 8)
            state = state.setValue(BlockStateProperties.WATERLOGGED, true);

        for (var dir : Direction.values()) {
            var prop = PipeBlock.DIRECTIONS.get(dir);
            var type = this.getConnectionType(world, pos, dir, state);
            // don't reconnect on blocked faces
            if (type.isConnected() && curr.getValue(prop) == Connection.BLOCKED)
                type = Connection.BLOCKED;
            state = state.setValue(prop, type);
        }
        return state;
    }

    protected Connection getConnectionType(Level world, BlockPos pos, Direction direction, BlockState state) {
        var offset = pos.relative(direction);
        if (!world.isLoaded(offset))
            return Connection.DISCONNECTED;
        var opposite = direction.getOpposite();
        var tile = world.getBlockEntity(offset);
        if (tile != null) {
            var connectable = tile.getCapability(Registry.pipeConnectableCapability, opposite).resolve();
            if (connectable.isPresent()) {
                return connectable.get().getConnectionType(pos, direction);
            }

            var handler = tile.getCapability(ForgeCapabilities.ITEM_HANDLER, opposite).resolve();
            if (handler.isPresent()) {
                return Connection.EXTERNAL;
            }
        }

        var blockHandler = Utility.getBlockItemHandler(world, offset, opposite);
        if (blockHandler != null) {
            return Connection.CONNECTED;
        }

//        var offState = world.getBlockState(offset);
//        if (PipeBlock.hasLegsTo(world, offState, offset, direction)) {
//            if (PipeBlock.DIRECTIONS.values().stream().noneMatch(d -> state.getValue(d) == Connection.LEGS))
//                return Connection.LEGS;
//        }
        return Connection.DISCONNECTED;
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@Nonnull Level level, @Nonnull BlockState state, @Nonnull BlockEntityType<T> type) {
        return BaseEntityBlock.createTickerHelper(type, Registry.pipeBlockEntity, PipeBlockEntity::tick);
    }

    public static void onStateChanged(Level world, BlockPos pos, BlockState newState) {
        // wait a few ticks before checking if we have to drop our modules, so that things like iron -> gold chest work
        var tile = Utility.getBlockEntity(PipeBlockEntity.class, world, pos);
        if (tile != null)
            tile.moduleDropCheck = 5;

        var network = PipeNetwork.get(world);
        var connections = 0;
        var force = false;
        for (var dir : Direction.values()) {
            var value = newState.getValue(PipeBlock.DIRECTIONS.get(dir));
            if (!value.isConnected())
                continue;
            connections++;
            var otherState = world.getBlockState(pos.relative(dir));
            // force a node if we're connecting to a different block (inventory etc.)
            if (otherState.getBlock() != newState.getBlock()) {
                force = true;
                break;
            }
        }
        if (force || connections > 2) {
            network.addNode(pos, newState);
        } else {
            network.removeNode(pos);
        }
        network.onPipeChanged(pos, newState);
    }
}
