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
import net.minecraft.world.level.block.SlabBlock;
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

import static net.minecraft.world.level.block.SlabBlock.TYPE;
import static net.minecraft.world.level.block.SlabBlock.WATERLOGGED;

public class SlabPistonBaseBlock extends BasicPistonBaseBlock implements SimpleWaterloggedBlock {
    protected static final VoxelShape BOTTOM_AABB = Block.box(0.0, 0.0, 0.0, 16.0, 8.0, 16.0);
    protected static final VoxelShape TOP_AABB = Block.box(0.0, 8.0, 0.0, 16.0, 16.0, 16.0);
    private static final BooleanProperty EXTENDED = BlockStateProperties.EXTENDED;
    public static final EnumProperty<Direction> FACING_TOP = EnumProperty.create("facing_top", Direction.class, Direction.Plane.HORIZONTAL);

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
        return switch (slabType) {
            case DOUBLE -> Shapes.block();
            case TOP -> TOP_AABB;
            default -> BOTTOM_AABB;
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
            BlockState blockState2 = this.defaultBlockState()
                    .setValue(TYPE, SlabType.BOTTOM)
                    .setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);
            Direction direction = ctx.getClickedFace();
            return direction != Direction.DOWN && (direction == Direction.UP || !(ctx.getClickLocation().y - (double)blockPos.getY() > 0.5))
                    ? blockState2.setValue(FACING, ctx.getHorizontalDirection().getOpposite())
                    : blockState2.setValue(TYPE, SlabType.TOP).setValue(FACING_TOP, ctx.getHorizontalDirection().getOpposite());
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
            case LAND -> false;
            case WATER -> blockState.getFluidState().is(FluidTags.WATER);
            case AIR -> false;
        };
    }
}
