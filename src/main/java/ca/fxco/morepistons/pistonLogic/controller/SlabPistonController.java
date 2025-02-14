package ca.fxco.morepistons.pistonLogic.controller;

import ca.fxco.morepistons.blocks.pistons.slabPiston.SlabMovingBlockEntity;
import ca.fxco.morepistons.blocks.pistons.slabPiston.SlabPistonBaseBlock;
import ca.fxco.morepistons.blocks.pistons.slabPiston.SlabPistonHeadBlock;
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
import ca.fxco.pistonlib.pistonLogic.structureRunners.BasicStructureRunner;
import ca.fxco.pistonlib.pistonLogic.structureRunners.MergingStructureRunner;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.piston.MovingPistonBlock;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import net.minecraft.world.level.block.piston.PistonStructureResolver;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.PistonType;
import net.minecraft.world.level.block.state.properties.SlabType;
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
        // TODO: Fix the type so that the super returns a basic structure runner, since it does...
        StructureRunner runner = super.newStructureRunner(level, pos, facing, length, extend, structureProvider);
        if (runner instanceof BasicStructureRunner basicStructureRunner) {
            return new SlabPistonStructureRunner(basicStructureRunner);
        }
        PistonFamily family = getFamily();
        PistonType type = getType();
        return new SlabPistonStructureRunner(PistonLibConfig.mergingApi ?
                new MergingStructureRunner(level, pos, facing, length, family, type, extend , structureProvider) :
                new BasicStructureRunner(level, pos, facing, length, family, type, extend , structureProvider));
    }

    @Override
    public boolean triggerEvent(BlockState state, Level level, BlockPos pos, int type, int data) {
        Direction facing = null;
        Direction facingTop = null;
        boolean isDifferentDirections;
        if ((data & 0b1000000) != 0) {
            int bottom = data & 7;
            int top = (data >> 3) & 7;
            if (bottom != 7) {
                facing = Direction.from3DDataValue(bottom);
            }

            if (top != 7) {
                facingTop = Direction.from3DDataValue(top);
            }
            isDifferentDirections = facing != facingTop;
        } else {
            facing = Direction.from3DDataValue(data);
            isDifferentDirections = false;
        }
        boolean isBottomMoved = facing != null;
        boolean isTopMoved = facingTop != null;
        if (!isBottomMoved && !isTopMoved) {
            return false;
        }

        if (!level.isClientSide()) {
            boolean shouldExtend = isBottomMoved && this.hasNeighborSignal(level, pos, facing);
            boolean shouldExtendTop = isTopMoved && this.hasNeighborSignal(level, pos, facingTop);

            if ((shouldExtend || shouldExtendTop) && PistonEvents.isRetract(type)) {
                level.setBlock(pos, state.setValue(EXTENDED, true)
                        .setValue(EXTENDED_TOP, true), UPDATE_CLIENTS);
                return false;
            }
            if (!shouldExtend && !shouldExtendTop && PistonEvents.isExtend(type)) {
                return false;
            }
        }

        int length = getLength(level, pos, state);
        int lengthTop = isTopMoved && isBottomMoved || !isDifferentDirections ?
                length : this.getTopLength(level, pos, state);

        if (PistonEvents.isExtend(type)) {
            boolean canMove = isBottomMoved && this.moveBlocks(level, pos, facing, length, true);
            boolean topCanMove = isTopMoved && lengthTop != getFamily().getMaxLength() &&
                    this.moveBlocks(level, pos, facingTop, lengthTop, true);

            if (!canMove && !topCanMove) {
                return false;
            }

            if (canMove && length > 0) {
                BlockPos armPos = pos.relative(facing, length);
                BlockState armState = getFamily().getArm().defaultBlockState().
                        setValue(BasicPistonArmBlock.FACING, facing).
                        setValue(BasicPistonArmBlock.SHORT, false);

                level.setBlock(armPos, armState, UPDATE_MOVE_BY_PISTON | UPDATE_ALL);
            }

            if (topCanMove && lengthTop > 0) {
                BlockPos armPos = pos.relative(facingTop, lengthTop);
                BlockState armState = getFamily().getArm().defaultBlockState().
                        setValue(BasicPistonArmBlock.FACING, facingTop).
                        setValue(BasicPistonArmBlock.SHORT, false);

                level.setBlock(armPos, armState, UPDATE_MOVE_BY_PISTON | UPDATE_ALL);
            }

            if ((isBottomMoved && isTopMoved || !isDifferentDirections) && length <= 0) {
                level.setBlock(pos, state.setValue(EXTENDED, true)
                        .setValue(EXTENDED_TOP, true), UPDATE_MOVE_BY_PISTON | UPDATE_ALL);
            } else if (isTopMoved && lengthTop <= 0) {
                level.setBlock(pos, state.setValue(EXTENDED_TOP, true)
                        .setValue(EXTENDED, state.getValue(EXTENDED)), UPDATE_MOVE_BY_PISTON | UPDATE_ALL);
            } else if (length <= 0) {
                level.setBlock(pos, state.setValue(EXTENDED, true)
                        .setValue(EXTENDED_TOP, state.getValue(EXTENDED_TOP)), UPDATE_MOVE_BY_PISTON | UPDATE_ALL);
            }

            playEvents(level, GameEvent.BLOCK_ACTIVATE, pos);
        } else if (PistonEvents.isRetract(type)) {
            if (!isBottomMoved) {
                facing = facingTop;
                length = lengthTop;
            } else if (isTopMoved) {
                level.setBlock(pos.relative(facingTop, lengthTop), Blocks.AIR.defaultBlockState(),
                        UPDATE_MOVE_BY_PISTON | UPDATE_KNOWN_SHAPE | UPDATE_INVISIBLE);
            }
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
            if (movingBaseBlockEntity instanceof SlabMovingBlockEntity slabMBE) {
                slabMBE.extendedSides = state.getValue(EXTENDED) ? state.getValue(EXTENDED_TOP)
                        ? SlabType.DOUBLE : SlabType.BOTTOM : SlabType.TOP;
            }
            level.setBlock(sourcePos, movingBaseState, UPDATE_MOVE_BY_PISTON | UPDATE_KNOWN_SHAPE | UPDATE_INVISIBLE);
            level.setBlockEntity(movingBaseBlockEntity);

            level.updateNeighborsAt(sourcePos, movingBaseState.getBlock());
            movingBaseState.updateNeighbourShapes(level, sourcePos, UPDATE_CLIENTS);

            if (pistonType == PistonType.STICKY) {
                boolean droppedBlock;
                BlockPos frontPos;
                BlockState frontState;
                if (isBottomMoved) {
                    droppedBlock = false;
                    frontPos = pos.relative(facing, length + 1);
                    frontState = level.getBlockState(frontPos);

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
                }

                if (isTopMoved) {
                    droppedBlock = false;
                    headPos = pos.relative(facingTop, lengthTop);
                    frontPos = pos.relative(facingTop, lengthTop + 1);
                    frontState = level.getBlockState(frontPos);

                    if (frontState.is(family.getMoving())) {
                        BlockEntity frontBlockEntity = level.getBlockEntity(frontPos);

                        if (frontBlockEntity instanceof PistonMovingBlockEntity mbe &&
                                mbe.getDirection() == facingTop && mbe.isExtending()) {
                            mbe.finalTick();
                            droppedBlock = true;
                        } else if (frontBlockEntity instanceof MergeBlockEntity mbe) {
                            PLMergeBlockEntity.MergeData mergeData = mbe.getMergingBlocks().get(facingTop);
                            mergeData.setProgress(1F);
                            droppedBlock = true;
                        }
                    }

                    if (!droppedBlock) {
                        if (type == PistonEvents.RETRACT_NO_PULL || frontState.isAir() ||
                                (frontState.getPistonPushReaction() != PushReaction.NORMAL &&
                                        !frontState.is(ModTags.PISTONS)) ||
                                !canMoveBlock(frontState, level, frontPos, facingTop.getOpposite(), false, facingTop)) {
                            if (!PistonLibConfig.illegalBreakingFix ||
                                    level.getBlockState(headPos).getDestroySpeed(level, headPos) != -1.0F) {
                                level.removeBlock(headPos, false);
                            }
                        } else {
                            this.moveBlocks(level, pos, facingTop, lengthTop, false);
                        }
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
        PistonFamily family = this.getFamily();
        if (!state.getValue(EXTENDED_TOP)) {
            return family.getMinLength();
        } else {
            int maxLength = family.getMaxLength();
            if (maxLength == 1) {
                return 1;
            } else {
                Direction facing = state.getValue(FACING_TOP);
                int length = family.getMinLength();

                while(length++ < maxLength) {
                    BlockPos frontPos = pos.relative(facing, length);
                    BlockState frontState = level.getBlockState(frontPos);
                    if (!frontState.is(family.getArm())) {
                        break;
                    }
                }

                return length;
            }
        }
    }

    @Override
    public void checkIfExtend(Level level, BlockPos pos, BlockState state, boolean onPlace) {
        if (level.isClientSide()) {
            return;
        }

        Direction facing = state.getValue(FACING);
        Direction facingTop = state.getValue(FACING_TOP);

        if (state.getValue(SlabPistonBaseBlock.TYPE) != SlabType.DOUBLE || facing == facingTop) {
            super.checkIfExtend(level, pos, state, onPlace);
            return;
        }

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
        int type = PistonEvents.NONE;
        int bottom = facing.get3DDataValue() & 7;
        int top = facingTop.get3DDataValue() & 7;
        int packed = 0b1000000;

        if (shouldExtend && length < family.getMaxLength()) {
            type = PistonEvents.EXTEND;
            if (this.newStructureResolver(level, pos, facing, length, true).resolve()) {
                packed |= bottom;
            } else {
                packed |= 7;
            }
        } else if (!shouldExtend && length > family.getMinLength()) {
            type = getRetractType((ServerLevel)level, pos, facing, length);
            packed = packed | bottom;
        } else {
            packed |= 7;
        }

        if (shouldExtendTop && lengthTop < family.getMaxLength()) {
            type = PistonEvents.EXTEND;
            if (this.newStructureResolver(level, pos, facingTop, lengthTop, true).resolve()) {
                packed |= (top << 3);
            } else {
                packed |= 0b111000;
            }
        } else if (!shouldExtendTop && lengthTop > family.getMinLength()) {
            type = getRetractType((ServerLevel)level, pos, facingTop, lengthTop);
            packed |= (top << 3);
        } else {
            packed |= 0b111000;
        }

        if (type != PistonEvents.NONE) {
            level.blockEvent(pos, state.getBlock(), type, packed);
        }
    }

    @Override
    public BlockState getHeadState(BlockPos pistonPos, Level level, Direction pushingDir) {
        BlockState pistonState;
        if (level.getBlockEntity(pistonPos) instanceof SlabMovingBlockEntity entity) {
            pistonState = entity.getMovedState();
        } else {
            pistonState = level.getBlockState(pistonPos);
        }

        Direction pistonFacing = pistonState.getValue(SlabPistonBaseBlock.FACING);
        Direction pistonFacingTop = pistonState.getValue(SlabPistonBaseBlock.FACING_TOP);
        SlabType slabType = pistonState.getValue(BlockStateProperties.SLAB_TYPE);
        if (slabType == SlabType.DOUBLE && pistonFacing != pistonFacingTop) {
            if (pushingDir == pistonFacing) {
                slabType = SlabType.BOTTOM;
            } else {
                slabType = SlabType.TOP;
            }
        }

        return getFamily().getHead().defaultBlockState()
                .setValue(BasicPistonHeadBlock.TYPE, getType())
                .setValue(BasicPistonHeadBlock.FACING, pushingDir)
                .setValue(SlabPistonHeadBlock.SLAB_TYPE, slabType);
    }

}
