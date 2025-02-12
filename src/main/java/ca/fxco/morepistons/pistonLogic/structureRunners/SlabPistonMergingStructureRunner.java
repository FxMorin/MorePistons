package ca.fxco.morepistons.pistonLogic.structureRunners;

import ca.fxco.morepistons.blocks.pistons.slabPiston.SlabPistonBaseBlock;
import ca.fxco.morepistons.blocks.pistons.slabPiston.SlabPistonHeadBlock;
import ca.fxco.pistonlib.api.pistonLogic.families.PistonFamily;
import ca.fxco.pistonlib.api.pistonLogic.structure.StructureResolver;
import ca.fxco.pistonlib.blocks.pistons.basePiston.BasicMovingBlock;
import ca.fxco.pistonlib.blocks.pistons.basePiston.BasicPistonHeadBlock;
import ca.fxco.pistonlib.pistonLogic.structureRunners.MergingStructureRunner;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.piston.PistonStructureResolver;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.PistonType;
import net.minecraft.world.level.block.state.properties.SlabType;

import static net.minecraft.world.level.block.Block.UPDATE_INVISIBLE;
import static net.minecraft.world.level.block.Block.UPDATE_MOVE_BY_PISTON;

public class SlabPistonMergingStructureRunner extends MergingStructureRunner {

    public <S extends PistonStructureResolver & StructureResolver> SlabPistonMergingStructureRunner(
            Level level, BlockPos pos, Direction facing, int length, PistonFamily family, PistonType type,
            boolean extend, StructureResolver.Factory<S> structureProvider
    ) {
        super(level, pos, facing, length, family, type, extend, structureProvider);
    }

    @Override
    public void taskPlaceExtendingHead() {
        if (extend) {
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
            BlockPos headPos = blockPos.relative(facing, length + 1);
            BlockState headState = this.family.getHead().defaultBlockState()
                    .setValue(BasicPistonHeadBlock.TYPE, this.type)
                    .setValue(BasicPistonHeadBlock.FACING, facing)
                    .setValue(SlabPistonHeadBlock.SLAB_TYPE, slabType);

            toRemove.remove(headPos);

            BlockState movingBlock = this.family.getMoving().defaultBlockState()
                    .setValue(BasicMovingBlock.FACING, facing);
            BlockEntity movingBlockEntity = this.family
                    .newMovingBlockEntity(headPos, movingBlock, headState, null, facing, extend, true);

            level.setBlock(headPos, movingBlock, UPDATE_MOVE_BY_PISTON | UPDATE_INVISIBLE);
            level.setBlockEntity(movingBlockEntity);
        }
    }
}
