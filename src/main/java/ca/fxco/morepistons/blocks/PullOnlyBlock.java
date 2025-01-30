package ca.fxco.morepistons.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class PullOnlyBlock extends Block {

	public PullOnlyBlock(Properties properties) {
        super(properties);
    }

    @Override
    public boolean pl$usesConfigurablePistonBehavior() {
        return true;
    }

    @Override
    public boolean pl$canPistonPush(BlockGetter level, BlockPos pos, BlockState state, Direction dir) {
        return false;
    }
}
