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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.piston.MovingPistonBlock;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import net.minecraft.world.level.block.piston.PistonStructureResolver;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.PistonType;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.PushReaction;

import static ca.fxco.morepistons.blocks.pistons.slabPiston.SlabPistonBaseBlock.EXTENDED_TOP;
import static ca.fxco.morepistons.blocks.pistons.slabPiston.SlabPistonBaseBlock.FACING_TOP;
import static net.minecraft.world.level.block.Block.*;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.EXTENDED;
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
        Direction facing = Direction.from3DDataValue(data);
        Direction facingBottom = state.getValue(FACING);
        Direction facingTop = state.getValue(FACING_TOP);
        if (!level.isClientSide()) {
            boolean shouldExtend = this.hasNeighborSignal(level, pos, facing);

            if (shouldExtend && PistonEvents.isRetract(type)) {
                if (facingBottom == facingTop) {
                    level.setBlock(pos, state.setValue(EXTENDED, true)
                            .setValue(EXTENDED_TOP, true), UPDATE_CLIENTS);
                } else if (facing == facingTop) {
                    level.setBlock(pos, state.setValue(EXTENDED_TOP, true)
                            .setValue(EXTENDED, state.getValue(EXTENDED)), UPDATE_CLIENTS);
                } else {
                    level.setBlock(pos, state.setValue(EXTENDED, true)
                            .setValue(EXTENDED_TOP, state.getValue(EXTENDED_TOP)), UPDATE_CLIENTS);
                }

                return false;
            }
            if (!shouldExtend && PistonEvents.isExtend(type)) {
                return false;
            }
        }

        int length = facingTop == facing ? this.getTopLength(level, pos, state) : getLength(level, pos, state);

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
                if (facingBottom == facingTop) {
                    level.setBlock(pos, state.setValue(EXTENDED, true)
                            .setValue(EXTENDED_TOP, true), UPDATE_MOVE_BY_PISTON | UPDATE_ALL);
                } else if (facing == facingTop) {
                    level.setBlock(pos, state.setValue(EXTENDED_TOP, true)
                            .setValue(EXTENDED, state.getValue(EXTENDED)), UPDATE_MOVE_BY_PISTON | UPDATE_ALL);
                } else {
                    level.setBlock(pos, state.setValue(EXTENDED, true)
                            .setValue(EXTENDED_TOP, state.getValue(EXTENDED_TOP)), UPDATE_MOVE_BY_PISTON | UPDATE_ALL);
                }
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
                    .setValue(FACING, state.getValue(SlabPistonBaseBlock.FACING))
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

    public int getTopLength(Level level, BlockPos pos, BlockState state) {
        return state.getValue(EXTENDED_TOP) ? this.getFamily().getMaxLength() : this.getFamily().getMinLength();
    }

    @Override
    public void checkIfExtend(Level level, BlockPos pos, BlockState state, boolean onPlace) {
        if (level.isClientSide()) {
            return;
        }

        Direction facing = state.getValue(FACING);
        Direction facingTop = state.getValue(FACING_TOP);
        int length = this.getLength(level, pos, state);
        int lengthTop = this.getTopLength(level, pos, state);
        boolean shouldExtend = hasNeighborSignal(level, pos, facing);
        boolean shouldExtendTop = hasNeighborSignal(level, pos, facingTop);

        PistonFamily family = this.getFamily();
        if (PistonLibConfig.headlessPistonFix && !onPlace) {
            if (length > family.getMinLength()) {
                BlockState blockState = level.getBlockState(pos.relative(facing, length));
                if (shouldExtend && !blockState.is(family.getMoving()) && !blockState.is(family.getHead())) {
                    level.removeBlock(pos, false);
                    ItemEntity itemEntity = new ItemEntity(
                            level,
                            pos.getX(), pos.getY(), pos.getZ(),
                            new ItemStack(family.getBase(getType()).asItem())
                    );
                    itemEntity.setDefaultPickUpDelay();
                    level.addFreshEntity(itemEntity);
                    return;
                }
            }
            if (lengthTop > family.getMinLength()) {
                BlockState blockState = level.getBlockState(pos.relative(facingTop, lengthTop));
                if (shouldExtend && !blockState.is(family.getMoving()) && !blockState.is(family.getHead())) {
                    level.removeBlock(pos, false);
                    ItemEntity itemEntity = new ItemEntity(
                            level,
                            pos.getX(), pos.getY(), pos.getZ(),
                            new ItemStack(family.getBase(getType()).asItem())
                    );
                    itemEntity.setDefaultPickUpDelay();
                    level.addFreshEntity(itemEntity);
                    return;
                }
            }
        }

        if (shouldExtend && length < family.getMaxLength()) {
            if (this.newStructureResolver(level, pos, facing, length, true).resolve()) {
                level.blockEvent(pos, state.getBlock(), PistonEvents.EXTEND, facing.get3DDataValue());
            }
        } else if (!shouldExtend && length > family.getMinLength()) {
            int type = getRetractType((ServerLevel)level, pos, facing, length);
            if (type != PistonEvents.NONE) {
                level.blockEvent(pos, state.getBlock(), type, facing.get3DDataValue());
            }
        }

        if (shouldExtendTop && lengthTop < family.getMaxLength()) {
            if (this.newStructureResolver(level, pos, facingTop, lengthTop, true).resolve()) {
                level.blockEvent(pos, state.getBlock(), PistonEvents.EXTEND, facingTop.get3DDataValue());
            }
        } else if (!shouldExtendTop && lengthTop > family.getMinLength()) {
            int type = getRetractType((ServerLevel)level, pos, facingTop, lengthTop);
            if (type != PistonEvents.NONE) {
                level.blockEvent(pos, state.getBlock(), type, facingTop.get3DDataValue());
            }
        }
    }

}
