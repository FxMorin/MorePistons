package ca.fxco.morepistons.blocks.pistons.configurablePiston;

import ca.fxco.pistonlib.api.pistonLogic.sticky.StickyType;
import ca.fxco.pistonlib.blocks.pistons.basePiston.BasicPistonHeadBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

import java.util.Map;

public class ConfigurablePistonHeadBlock extends BasicPistonHeadBlock {

    public ConfigurablePistonHeadBlock(Properties properties) {
        super(properties);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (this.getFamily().isVerySticky()) {
            BlockState blockState = level.getBlockState(pos.relative(state.getValue(FACING).getOpposite()));
            return this.isFittingBase(state, blockState) || blockState.is(this.getFamily().getMoving());
        }
        return super.canSurvive(state, level, pos);
    }

    @Override
    public void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, TYPE, SHORT);
    }

    @Override
    public boolean pl$usesConfigurablePistonBehavior() {
        return this.getFamily().isVerySticky(); // Makes the piston head movable by bypassing vanilla checks
    }

    @Override
    public boolean pl$usesConfigurablePistonStickiness() {
        return this.getFamily().isVerySticky();
    }

    // Returns a list of directions that are sticky, and the stickyType.
    public Map<Direction, StickyType> pl$stickySides(BlockState state) {
        return Map.of(state.getValue(FACING), StickyType.STICKY,
                state.getValue(FACING).getOpposite(), StickyType.STICKY);
    }

    public StickyType pl$sideStickiness(BlockState state, Direction direction) {
        return state.getValue(FACING).getAxis() == direction.getAxis() ? StickyType.STICKY : StickyType.DEFAULT;
    }

    @Override
    public boolean isFittingBase(BlockState headState, BlockState behindState) {
        return behindState.is(this.getFamily().getArm()) ?
                behindState.getValue(FACING) == headState.getValue(FACING) :
                super.isFittingBase(headState, behindState);
    }
}
