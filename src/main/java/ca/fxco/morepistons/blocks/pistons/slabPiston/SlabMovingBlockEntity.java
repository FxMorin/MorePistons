package ca.fxco.morepistons.blocks.pistons.slabPiston;

import ca.fxco.pistonlib.api.pistonLogic.families.PistonFamily;
import ca.fxco.pistonlib.api.pistonLogic.structure.StructureGroup;
import ca.fxco.pistonlib.blocks.pistons.basePiston.BasicMovingBlockEntity;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

@Getter
public class SlabMovingBlockEntity extends BasicMovingBlockEntity {

    public SlabMovingBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    public SlabMovingBlockEntity(PistonFamily family, @Nullable StructureGroup structureGroup, BlockPos pos, BlockState state, BlockState movedState, BlockEntity movedBlockEntity, Direction facing, boolean extending, boolean isSourcePiston) {
        super(family, structureGroup, pos, state, movedState, movedBlockEntity, facing, extending, isSourcePiston);
    }

    public float getXOff(float f, Direction direction) {
        return (float)direction.getStepX() * this.getExtendedProgress(this.getProgress(f));
    }

    public float getYOff(float f, Direction direction) {
        return (float)direction.getStepY() * this.getExtendedProgress(this.getProgress(f));
    }

    public float getZOff(float f, Direction direction) {
        return (float)direction.getStepZ() * this.getExtendedProgress(this.getProgress(f));
    }

}
