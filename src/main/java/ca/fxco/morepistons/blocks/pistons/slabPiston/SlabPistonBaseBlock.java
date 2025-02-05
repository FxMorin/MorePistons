package ca.fxco.morepistons.blocks.pistons.slabPiston;

import ca.fxco.pistonlib.api.pistonLogic.controller.PistonController;
import ca.fxco.pistonlib.blocks.pistons.basePiston.BasicPistonBaseBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import static net.minecraft.world.level.block.SlabBlock.WATERLOGGED;

public class SlabPistonBaseBlock extends BasicPistonBaseBlock implements SimpleWaterloggedBlock {
    protected static final VoxelShape BOTTOM_AABB = Block.box(0.0, 0.0, 0.0, 16.0, 8.0, 16.0);
    protected static final VoxelShape TOP_AABB = Block.box(0.0, 8.0, 0.0, 16.0, 16.0, 16.0);
    private static final BooleanProperty EXTENDED = BlockStateProperties.EXTENDED;
    public static final EnumProperty<Direction> FACING_TOP = EnumProperty.create("facing_top", Direction.class, Direction.Plane.HORIZONTAL);
    public static final EnumProperty<SlabType> TYPE = BlockStateProperties.SLAB_TYPE;
    private static final Direction[] DIRECTIONS = {Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};
    protected static final VoxelShape[] BOTTOM_AABBS;
    protected static final VoxelShape[] TOP_AABBS;
    protected static final VoxelShape[][] DOUBLE_AABBS;

    public SlabPistonBaseBlock(PistonController controller, Properties properties) {
        super(controller, properties);
        this.registerDefaultState(this.defaultBlockState().setValue(TYPE, SlabType.BOTTOM).setValue(WATERLOGGED, Boolean.FALSE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(TYPE, WATERLOGGED, EXTENDED, FACING, FACING_TOP);
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState blockState) {
        return blockState.getValue(TYPE) != SlabType.DOUBLE;
    }

    @Override
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        SlabType slabType = blockState.getValue(TYPE);

        if (!blockState.getValue(EXTENDED)) {
            return switch (slabType) {
                case DOUBLE -> Shapes.block();
                case TOP -> TOP_AABB;
                default -> BOTTOM_AABB;
            };
        }
        Direction facing = blockState.getValue(FACING);
        if (facing.ordinal() < 2)
            return Shapes.empty();

        return switch (slabType) {
            case DOUBLE -> DOUBLE_AABBS[facing.ordinal() - 2][blockState.getValue(FACING_TOP).ordinal() - 2];
            case TOP -> TOP_AABBS[blockState.getValue(FACING_TOP).ordinal() - 2];
            default -> BOTTOM_AABBS[facing.ordinal() - 2];
        };
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        BlockPos blockPos = ctx.getClickedPos();
        BlockState blockState = ctx.getLevel().getBlockState(blockPos);
        if (blockState.is(this)) {
            if (blockState.getValue(TYPE) == SlabType.BOTTOM) {
                return blockState.setValue(TYPE, SlabType.DOUBLE).setValue(WATERLOGGED, Boolean.FALSE)
                        .setValue(FACING_TOP, ctx.getHorizontalDirection().getOpposite())
                        .setValue(FACING, blockState.getValue(FACING));
            } else {
                return blockState.setValue(TYPE, SlabType.DOUBLE).setValue(WATERLOGGED, Boolean.FALSE)
                        .setValue(FACING, ctx.getHorizontalDirection().getOpposite())
                        .setValue(FACING_TOP, blockState.getValue(FACING_TOP));
            }

        } else {
            FluidState fluidState = ctx.getLevel().getFluidState(blockPos);
            Direction facing = ctx.getHorizontalDirection().getOpposite();
            BlockState blockState2 = this.defaultBlockState()
                    .setValue(TYPE, SlabType.BOTTOM)
                    .setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER)
                    .setValue(FACING, facing)
                    .setValue(FACING_TOP, facing);
            Direction direction = ctx.getClickedFace();
            return direction != Direction.DOWN && (direction == Direction.UP ||
                    !(ctx.getClickLocation().y - (double)blockPos.getY() > 0.5)) ?
                    blockState2 : blockState2.setValue(TYPE, SlabType.TOP);
        }
    }

    @Override
    protected boolean canBeReplaced(BlockState blockState, BlockPlaceContext blockPlaceContext) {
        ItemStack itemStack = blockPlaceContext.getItemInHand();
        SlabType slabType = blockState.getValue(TYPE);
        if (slabType == SlabType.DOUBLE || !itemStack.is(this.asItem())) {
            return false;
        } else if (blockPlaceContext.replacingClickedOnBlock()) {
            boolean bl = blockPlaceContext.getClickLocation().y - (double)blockPlaceContext.getClickedPos().getY() > 0.5;
            Direction direction = blockPlaceContext.getClickedFace();
            return slabType == SlabType.BOTTOM
                    ? direction == Direction.UP || bl && direction.getAxis().isHorizontal()
                    : direction == Direction.DOWN || !bl && direction.getAxis().isHorizontal();
        } else {
            return true;
        }
    }

    @Override
    protected FluidState getFluidState(BlockState blockState) {
        return blockState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(blockState);
    }

    @Override
    public boolean placeLiquid(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState, FluidState fluidState) {
        return blockState.getValue(TYPE) != SlabType.DOUBLE && SimpleWaterloggedBlock.super.placeLiquid(levelAccessor, blockPos, blockState, fluidState);
    }

    @Override
    public boolean canPlaceLiquid(@Nullable Player player, BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, Fluid fluid) {
        return blockState.getValue(TYPE) != SlabType.DOUBLE && SimpleWaterloggedBlock.super.canPlaceLiquid(player, blockGetter, blockPos, blockState, fluid);
    }

    @Override
    protected BlockState updateShape(
            BlockState blockState,
            LevelReader levelReader,
            ScheduledTickAccess scheduledTickAccess,
            BlockPos blockPos,
            Direction direction,
            BlockPos blockPos2,
            BlockState blockState2,
            RandomSource randomSource
    ) {
        if (blockState.getValue(WATERLOGGED)) {
            scheduledTickAccess.scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelReader));
        }

        return super.updateShape(blockState, levelReader, scheduledTickAccess, blockPos, direction, blockPos2, blockState2, randomSource);
    }

    @Override
    public boolean isPathfindable(BlockState blockState, PathComputationType pathComputationType) {
        return switch (pathComputationType) {
            case LAND, AIR -> false;
            case WATER -> blockState.getFluidState().is(FluidTags.WATER);
        };
    }

    static {
        BOTTOM_AABBS = new VoxelShape[4];
        TOP_AABBS = new VoxelShape[4];
        DOUBLE_AABBS = new VoxelShape[4][4];

        for (Direction direction : DIRECTIONS) {
            float stepX = direction.getStepX() * 4;
            float stepZ = direction.getStepZ() * 4;
            BOTTOM_AABBS[direction.ordinal() - 2] = Block.box(
                    (-stepX < 0 ? 0 : -stepX),
                    0,
                    (-stepZ < 0 ? 0 : -stepZ),
                    16 + (-stepX > 0 ? 0 : -stepX),
                    8F,
                    16 + (-stepZ > 0 ? 0 : -stepZ));
        }
        for (Direction direction : DIRECTIONS) {
            float stepX = direction.getStepX() * 4;
            float stepZ = direction.getStepZ() * 4;
            TOP_AABBS[direction.ordinal() - 2] = Block.box(
                    (-stepX < 0 ? 0 : -stepX),
                    8f,
                    (-stepZ < 0 ? 0 : -stepZ),
                    16 + (-stepX > 0 ? 0 : -stepX),
                    16F,
                    16 + (-stepZ > 0 ? 0 : -stepZ));
        }
        for (Direction direction : DIRECTIONS) {
            for (Direction direction2 : DIRECTIONS) {
                DOUBLE_AABBS[direction.ordinal() - 2][direction2.ordinal() - 2] =
                        Shapes.or(BOTTOM_AABBS[direction.ordinal() - 2], TOP_AABBS[direction2.ordinal() - 2]);
            }
        }
    }
}
