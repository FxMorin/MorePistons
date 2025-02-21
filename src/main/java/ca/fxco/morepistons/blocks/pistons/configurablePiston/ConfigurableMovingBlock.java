package ca.fxco.morepistons.blocks.pistons.configurablePiston;

import ca.fxco.pistonlib.api.pistonLogic.controller.PistonController;
import ca.fxco.pistonlib.api.pistonLogic.sticky.StickyType;
import ca.fxco.pistonlib.blocks.pistons.basePiston.BasicMovingBlock;
import ca.fxco.pistonlib.blocks.pistons.basePiston.BasicMovingBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.redstone.Orientation;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ConfigurableMovingBlock extends BasicMovingBlock {

    public ConfigurableMovingBlock() {
        this(BasicMovingBlock.createDefaultSettings());
    }

    public ConfigurableMovingBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock,
                                Orientation orientation, boolean movedByPiston) {
        if (this.getFamily().isExtendOnRetracting() &&
                level.getBlockEntity(pos) instanceof BasicMovingBlockEntity movingBlockEntity &&
                movingBlockEntity.isSourcePiston &&
                movingBlockEntity.movedState.getBlock() instanceof ConfigurablePistonBaseBlock cpbb &&
                !movingBlockEntity.isExtending()) {
            Direction facing = movingBlockEntity.movedState.getValue(FACING);
            PistonController pistonController = cpbb.pl$getPistonController();
            if (pistonController.hasNeighborSignal(level, pos, facing)) {
                float progress = movingBlockEntity.progress;
                movingBlockEntity.finalTick(false, false);
                Set<BlockPos> positions = new HashSet<>();
                BlockPos frontPos = pos.relative(facing);
                if (level.getBlockEntity(frontPos) instanceof ConfigurableMovingBlockEntity bmbe &&
                        !bmbe.extending && bmbe.progress == progress) {
                    positions.add(frontPos);
                    if (bmbe.movedState.pl$usesConfigurablePistonStickiness() && bmbe.movedState.pl$isSticky()) {
                        stuckNeighbors(level, frontPos, bmbe.movedState.pl$stickySides(), bmbe, positions);
                    }
                    bmbe.finalTick();
                }
                pistonController.checkIfExtend(level, pos, movingBlockEntity.movedState, false);
                int progressInt = (int) ((1 - progress) * 255);
                level.blockEvent(frontPos, this, 99, progressInt);
                for (BlockPos pos9 : positions) {
                    level.blockEvent(pos9.relative(facing), this, 99, progressInt);
                }
            }
        }
    }

    private void stuckNeighbors(Level level, BlockPos pos, Map<Direction, StickyType> stickyTypes,
                                ConfigurableMovingBlockEntity thisMbe, Set<BlockPos> set) {
        for (Map.Entry<Direction, StickyType> entry : stickyTypes.entrySet()) {
            StickyType stickyType = entry.getValue();

            if (stickyType.ordinal() < StickyType.STRONG.ordinal()) { // only strong or fused
                continue;
            }

            Direction dir = entry.getKey();
            BlockPos neighborPos = pos.relative(dir);
            if (set.contains(neighborPos)) {
                continue;
            }
            BlockState neighborState = level.getBlockState(neighborPos);

            if (neighborState.is(this.getFamily().getMoving())) {
                BlockEntity blockEntity = level.getBlockEntity(neighborPos);

                if (blockEntity instanceof ConfigurableMovingBlockEntity mbe) {
                    if (!mbe.isExtending() && thisMbe.progress == mbe.progress) {
                        set.add(neighborPos);
                        if (mbe.movedState.pl$usesConfigurablePistonStickiness() && mbe.movedState.pl$isSticky()) {
                            stuckNeighbors(level, neighborPos, mbe.movedState.pl$stickySides(), mbe, set);
                        }
                        mbe.finalTick(true, true);
                    }
                }
            }
        }
    }

    public boolean triggerEvent(BlockState blockState, Level level, BlockPos blockPos, int type, int data) {
        if (type == 99 && level.getBlockEntity(blockPos) instanceof BasicMovingBlockEntity bmbe) {
            bmbe.progress = bmbe.progressO = data / 255F;
        }
        return true;
    }

    @Override
    public void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, TYPE);
    }
}
