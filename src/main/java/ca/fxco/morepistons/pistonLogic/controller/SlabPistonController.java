package ca.fxco.morepistons.pistonLogic.controller;

import ca.fxco.morepistons.blocks.pistons.slabPiston.SlabPistonBaseBlock;
import ca.fxco.morepistons.pistonLogic.structureRunners.SlabPistonStructureRunner;
import ca.fxco.pistonlib.PistonLibConfig;
import ca.fxco.pistonlib.api.pistonLogic.PistonEvents;
import ca.fxco.pistonlib.api.pistonLogic.base.PLMergeBlockEntity;
import ca.fxco.pistonlib.api.pistonLogic.families.PistonFamily;
import ca.fxco.pistonlib.api.pistonLogic.structure.StructureResolver;
import ca.fxco.pistonlib.api.pistonLogic.structure.StructureRunner;
import ca.fxco.pistonlib.base.ModTags;
import ca.fxco.pistonlib.blocks.mergeBlock.MergeBlockEntity;
import ca.fxco.pistonlib.blocks.pistons.basePiston.BasicPistonArmBlock;
import ca.fxco.pistonlib.blocks.pistons.basePiston.BasicPistonHeadBlock;
import ca.fxco.pistonlib.pistonLogic.controller.VanillaPistonController;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.piston.MovingPistonBlock;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import net.minecraft.world.level.block.piston.PistonStructureResolver;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.PistonType;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.PushReaction;

import static net.minecraft.world.level.block.Block.*;
import static net.minecraft.world.level.block.piston.PistonBaseBlock.EXTENDED;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING;

public class SlabPistonController extends VanillaPistonController {

    public SlabPistonController(PistonType type) {
        super(type);
    }

    @Override
    public <S extends PistonStructureResolver & StructureResolver> StructureRunner newStructureRunner(
            Level level, BlockPos pos, Direction facing, int length,
            boolean extend, StructureResolver.Factory<S> structureProvider
    ) {
        return new SlabPistonStructureRunner(level, pos, facing, length, getFamily(), getType(), extend, structureProvider); //TODO add merge variant
    }

    @Override
    public boolean triggerEvent(BlockState state, Level level, BlockPos pos, int type, int data) {
        Direction facing = state.getValue(FACING);
        Direction facingTop = state.getValue(SlabPistonBaseBlock.FACING_TOP);
        switch (state.getValue(SlabPistonBaseBlock.TYPE)) {
            case DOUBLE -> {
                if (facing == facingTop) {
                    return triggerEventForSide(state, level, pos, type, data, facing, SlabType.DOUBLE);
                }

                return triggerEventForSide(state, level, pos, type, data, facing, SlabType.BOTTOM) &&
                        triggerEventForSide(state, level, pos, type, data, facingTop, SlabType.TOP);
            }
            case TOP -> {
                return triggerEventForSide(state, level, pos, type, data, facingTop, SlabType.TOP);
            }
            default -> {
                return triggerEventForSide(state, level, pos, type, data, facing, SlabType.BOTTOM);
            }
        }
    }

    public boolean triggerEventForSide(BlockState state, Level level, BlockPos pos, int type, int data, Direction facing, SlabType armType) {
        if (!level.isClientSide()) {
            boolean shouldExtend = this.hasNeighborSignal(level, pos, facing);

            if (shouldExtend && PistonEvents.isRetract(type)) {
                level.setBlock(pos, state.setValue(EXTENDED, true), UPDATE_CLIENTS);
                return false;
            }
            if (!shouldExtend && PistonEvents.isExtend(type)) {
                return false;
            }
        }

        int length = this.getLength(level, pos, state);

        if (PistonEvents.isExtend(type)) {
            if (!this.moveBlocks(level, pos, facing, length, true)) {
                return false;
            }

            if (length > 0) {
                BlockPos armPos = pos.relative(facing, length);
                BlockState armState = getFamily().getArm().defaultBlockState().
                        setValue(BasicPistonArmBlock.FACING, facing).
                        setValue(BasicPistonArmBlock.SHORT, false);

                level.setBlock(armPos, armState, UPDATE_MOVE_BY_PISTON | UPDATE_ALL);
            } else {
                level.setBlock(pos, state.setValue(EXTENDED, true), UPDATE_MOVE_BY_PISTON | UPDATE_ALL);
            }

            playEvents(level, GameEvent.BLOCK_ACTIVATE, pos);
        } else if (PistonEvents.isRetract(type)) {
            BlockPos headPos = pos.relative(facing, length);
            BlockEntity headBlockEntity = level.getBlockEntity(headPos);

            if (headBlockEntity instanceof PistonMovingBlockEntity mbe) {
                mbe.finalTick();
            }

            PistonFamily family = getFamily();
            PistonType pistonType = getType();
            int newLength = length - 1;
            BlockPos sourcePos = pos.relative(facing, newLength);
            BlockState sourceState = (newLength > 0)
                    ? family.getHead().defaultBlockState()
                    .setValue(BasicPistonHeadBlock.FACING, Direction.from3DDataValue(data & 7))
                    .setValue(BasicPistonHeadBlock.TYPE, pistonType)
                    : state.getBlock().defaultBlockState()
                    .setValue(FACING, Direction.from3DDataValue(data & 7))
                    .setValue(SlabPistonBaseBlock.FACING_TOP, state.getValue(SlabPistonBaseBlock.FACING_TOP))
                    .setValue(SlabPistonBaseBlock.TYPE, state.getValue(SlabPistonBaseBlock.TYPE));

            BlockState movingBaseState = family.getMoving().defaultBlockState()
                    .setValue(MovingPistonBlock.FACING, facing)
                    .setValue(MovingPistonBlock.TYPE, pistonType);
            BlockEntity movingBaseBlockEntity = family.newMovingBlockEntity(
                    sourcePos,
                    movingBaseState,
                    sourceState,
                    null,
                    facing,
                    false,
                    true
            );
            level.setBlock(sourcePos, movingBaseState, UPDATE_MOVE_BY_PISTON | UPDATE_KNOWN_SHAPE | UPDATE_INVISIBLE);
            level.setBlockEntity(movingBaseBlockEntity);

            level.updateNeighborsAt(sourcePos, movingBaseState.getBlock());
            movingBaseState.updateNeighbourShapes(level, sourcePos, UPDATE_CLIENTS);

            if (pistonType == PistonType.STICKY) {
                boolean droppedBlock = false;

                BlockPos frontPos = pos.relative(facing, length + 1);
                BlockState frontState = level.getBlockState(frontPos);

                if (frontState.is(family.getMoving())) {
                    BlockEntity frontBlockEntity = level.getBlockEntity(frontPos);

                    if (frontBlockEntity instanceof PistonMovingBlockEntity mbe &&
                            mbe.getDirection() == facing && mbe.isExtending()) {
                        mbe.finalTick();
                        droppedBlock = true;
                    } else if (frontBlockEntity instanceof MergeBlockEntity mbe) {
                        PLMergeBlockEntity.MergeData mergeData = mbe.getMergingBlocks().get(facing);
                        mergeData.setProgress(1F);
                        droppedBlock = true;
                    }
                }
                if (!droppedBlock) {
                    if (type == PistonEvents.RETRACT_NO_PULL || frontState.isAir() ||
                            (frontState.getPistonPushReaction() != PushReaction.NORMAL &&
                                    !frontState.is(ModTags.PISTONS)) ||
                            !canMoveBlock(frontState, level, frontPos, facing.getOpposite(), false, facing)) {
                        if (!PistonLibConfig.illegalBreakingFix ||
                                level.getBlockState(headPos).getDestroySpeed(level, headPos) != -1.0F) {
                            level.removeBlock(headPos, false);
                        }
                    } else {
                        this.moveBlocks(level, pos, facing, length, false);
                    }
                }
            } else {
                if (!PistonLibConfig.illegalBreakingFix ||
                        level.getBlockState(headPos).getDestroySpeed(level, headPos) != -1.0F) {
                    level.removeBlock(headPos, false);
                }
            }

            playEvents(level, GameEvent.BLOCK_DEACTIVATE, pos);
        }

        return true;
    }
}
