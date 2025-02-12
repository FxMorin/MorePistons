package ca.fxco.morepistons.pistonLogic.structureRunners;

import ca.fxco.morepistons.blocks.pistons.slabPiston.SlabPistonBaseBlock;
import ca.fxco.morepistons.blocks.pistons.slabPiston.SlabPistonHeadBlock;
import ca.fxco.pistonlib.api.pistonLogic.families.PistonFamily;
import ca.fxco.pistonlib.api.pistonLogic.structure.StructureRunner;
import ca.fxco.pistonlib.blocks.pistons.basePiston.BasicMovingBlock;
import ca.fxco.pistonlib.blocks.pistons.basePiston.BasicPistonHeadBlock;
import ca.fxco.pistonlib.pistonLogic.structureRunners.BasicStructureRunner;
import lombok.experimental.Delegate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.SlabType;

import static net.minecraft.world.level.block.Block.UPDATE_INVISIBLE;
import static net.minecraft.world.level.block.Block.UPDATE_MOVE_BY_PISTON;

public class SlabPistonStructureRunner implements StructureRunner {

    @Delegate(types = StructureRunner.class)
    private final BasicStructureRunner delegate;

    public SlabPistonStructureRunner(BasicStructureRunner delegate) {
        this.delegate = delegate;
    }

    @Override
    public void taskPlaceExtendingHead() {
        if (delegate.isExtend()) {
            Level level = delegate.getLevel();
            Direction facing = delegate.getFacing();
            BlockPos blockPos = delegate.getBlockPos();
            PistonFamily family = this.delegate.getFamily();
            BlockState pistonState = level.getBlockState(blockPos);
            Direction pistonFacing = pistonState.getValue(SlabPistonBaseBlock.FACING);
            Direction pistonFacingTop = pistonState.getValue(SlabPistonBaseBlock.FACING_TOP);
            SlabType slabType = pistonState.getValue(BlockStateProperties.SLAB_TYPE);
            if (slabType == SlabType.DOUBLE && pistonFacing != pistonFacingTop) {
                if (facing == pistonFacing) {
                    slabType = SlabType.BOTTOM;
                } else {
                    slabType = SlabType.TOP;
                }
            }
            BlockPos headPos = blockPos.relative(facing, delegate.getLength() + 1);
            BlockState headState = family.getHead().defaultBlockState()
                    .setValue(BasicPistonHeadBlock.TYPE, delegate.getType())
                    .setValue(BasicPistonHeadBlock.FACING, facing)
                    .setValue(SlabPistonHeadBlock.SLAB_TYPE, slabType);

            delegate.getToRemove().remove(headPos);

            BlockState movingBlock = family.getMoving().defaultBlockState()
                    .setValue(BasicMovingBlock.FACING, facing);
            BlockEntity movingBlockEntity = family
                    .newMovingBlockEntity(headPos, movingBlock, headState, null, facing, true, true);

            level.setBlock(headPos, movingBlock, UPDATE_MOVE_BY_PISTON | UPDATE_INVISIBLE);
            level.setBlockEntity(movingBlockEntity);
        }
    }

}
