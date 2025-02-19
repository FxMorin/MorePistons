package ca.fxco.morepistons.blocks;

import ca.fxco.pistonlib.PistonLib;
import ca.fxco.pistonlib.api.pistonLogic.sticky.StickRules;
import ca.fxco.pistonlib.api.pistonLogic.sticky.StickyGroup;
import ca.fxco.pistonlib.api.pistonLogic.sticky.StickyType;
import ca.fxco.pistonlib.base.ModStickyGroups;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HoneyBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static ca.fxco.morepistons.utils.HalfBlockUtils.SIDES_LIST;
import static net.minecraft.world.level.block.SlabBlock.WATERLOGGED;

public class HoneySlabBlock extends HoneyBlock implements SimpleWaterloggedBlock {
    protected static final VoxelShape BOTTOM_SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 8.0, 16.0);
    protected static final VoxelShape TOP_SHAPE = Block.box(0.0, 8.0, 0.0, 16.0, 16.0, 16.0);
    protected static final VoxelShape BOTTOM_COLLISION_SHAPE = Block.box(1.0, 1.0, 1.0, 15.0, 7.0, 15.0);
    protected static final VoxelShape TOP_COLLISION_SHAPE = Block.box(1.0, 9.0, 1.0, 15.0, 15.0, 15.0);

    public static final EnumProperty<SlabType> TYPE = BlockStateProperties.SLAB_TYPE;
    public static final Map<Direction, StickyType> STICKY_SIDES = new HashMap<>();

    public HoneySlabBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(TYPE, SlabType.BOTTOM)
                .setValue(WATERLOGGED, false));
    }

    @Override
    protected boolean useShapeForLightOcclusion(BlockState blockState) {
        return blockState.getValue(TYPE) != SlabType.DOUBLE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(TYPE, WATERLOGGED);
    }

    @Override
    protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        SlabType slabType = blockState.getValue(TYPE);
        return switch (slabType) {
            case DOUBLE -> Shapes.block();
            case TOP -> TOP_SHAPE;
            default -> BOTTOM_SHAPE;
        };
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState blockState, BlockGetter blockGetter,
                                           BlockPos blockPos, CollisionContext collisionContext) {
        SlabType slabType = blockState.getValue(TYPE);
        return switch (slabType) {
            case DOUBLE -> SHAPE;
            case TOP -> TOP_COLLISION_SHAPE;
            default -> BOTTOM_COLLISION_SHAPE;
        };
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        BlockPos blockPos = blockPlaceContext.getClickedPos();
        BlockState blockState = blockPlaceContext.getLevel().getBlockState(blockPos);
        if (blockState.is(this)) {
            return blockState.setValue(TYPE, SlabType.DOUBLE).setValue(WATERLOGGED, false);
        } else {
            FluidState fluidState = blockPlaceContext.getLevel().getFluidState(blockPos);
            BlockState blockState2 = this.defaultBlockState()
                    .setValue(TYPE, SlabType.BOTTOM)
                    .setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);
            Direction direction = blockPlaceContext.getClickedFace();
            return direction != Direction.DOWN && (direction == Direction.UP ||
                    !(blockPlaceContext.getClickLocation().y - blockPos.getY() > 0.5))
                    ? blockState2
                    : blockState2.setValue(TYPE, SlabType.TOP);
        }
    }

    @Override
    protected boolean canBeReplaced(BlockState blockState, BlockPlaceContext blockPlaceContext) {
        ItemStack itemStack = blockPlaceContext.getItemInHand();
        SlabType slabType = blockState.getValue(TYPE);
        if (slabType == SlabType.DOUBLE || !itemStack.is(this.asItem())) {
            return false;
        } else if (blockPlaceContext.replacingClickedOnBlock()) {
            boolean bl = blockPlaceContext.getClickLocation().y - blockPlaceContext.getClickedPos().getY() > 0.5;
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
        return blockState.getValue(TYPE) != SlabType.DOUBLE &&
                SimpleWaterloggedBlock.super.placeLiquid(levelAccessor, blockPos, blockState, fluidState);
    }

    @Override
    public boolean canPlaceLiquid(@Nullable Player player, BlockGetter blockGetter,
                                  BlockPos blockPos, BlockState blockState, Fluid fluid) {
        return blockState.getValue(TYPE) != SlabType.DOUBLE &&
                SimpleWaterloggedBlock.super.canPlaceLiquid(player, blockGetter, blockPos, blockState, fluid);
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
        if ((Boolean)blockState.getValue(WATERLOGGED)) {
            scheduledTickAccess.scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelReader));
        }

        return super.updateShape(blockState, levelReader, scheduledTickAccess, blockPos, direction, blockPos2, blockState2, randomSource);
    }

    @Override
    protected boolean isPathfindable(BlockState blockState, PathComputationType pathComputationType) {
        switch (pathComputationType) {
            case LAND:
                return false;
            case WATER:
                return blockState.getFluidState().is(FluidTags.WATER);
            case AIR:
                return false;
            default:
                return false;
        }
    }

    @Override
    public boolean pl$usesConfigurablePistonStickiness() {
        return true;
    }

    @Override
    public @Nullable StickyGroup pl$getStickyGroup(BlockState state) {
        return ModStickyGroups.SLIME;
    }

    @Override
    public Map<Direction, StickyType> pl$stickySides(BlockState state) {
        return switch (state.getValue(TYPE)) {
            case TOP -> SIDES_LIST[Direction.DOWN.ordinal()];
            case BOTTOM -> SIDES_LIST[Direction.UP.ordinal()];
            case DOUBLE -> STICKY_SIDES;
        };
    }

    @Override
    public StickyType pl$sideStickiness(BlockState state, Direction dir) {
        switch (state.getValue(TYPE)) {
            case TOP -> {
                if (dir == Direction.UP) {
                    return StickyType.STICKY;
                } else if (dir == Direction.DOWN) {
                    return StickyType.DEFAULT;
                }
            }
            case BOTTOM -> {
                if (dir == Direction.DOWN) {
                    return StickyType.STICKY;
                } else if (dir == Direction.UP) {
                    return StickyType.DEFAULT;
                }
            }
            case DOUBLE -> {
                return StickyType.STICKY;
            }
        }

        return StickyType.CONDITIONAL;
    }

    // Only the sides call the conditional check
    @Override
    public boolean pl$matchesStickyConditions(BlockState state, BlockState neighborState, Direction dir) {
        SlabType slabType = state.getValue(TYPE);
        if (slabType != SlabType.DOUBLE) {
            Optional<SlabType> neighborSlabType = neighborState.getOptionalValue(TYPE);
            if (neighborSlabType.isPresent()) {
                return neighborSlabType.get() == slabType || neighborSlabType.get() == SlabType.DOUBLE;
            }
        }
        StickyGroup group = neighborState.pl$getStickyGroup();
        if (group != null) {
            return StickRules.test(ModStickyGroups.HONEY, group);
        }
        return true;
    }

    static {
        for (Direction direction : PistonLib.DIRECTIONS) {
            STICKY_SIDES.put(direction, StickyType.STICKY);
        }
    }

    @Override
    public boolean pl$canUnMerge(BlockState state, BlockGetter level, BlockPos pos, BlockState neighborState, Direction dir) {
        return false;
    }

}
