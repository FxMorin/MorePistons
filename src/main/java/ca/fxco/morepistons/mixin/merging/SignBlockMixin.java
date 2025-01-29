package ca.fxco.morepistons.mixin.merging;

import ca.fxco.morepistons.MorePistonsConfig;
import ca.fxco.pistonlib.api.block.PLBlockBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SignBlock.class)
public class SignBlockMixin implements PLBlockBehaviour {

    @Override
    public boolean pl$usesConfigurablePistonMerging() {
        return MorePistonsConfig.doSignMerging;
    }

    @Override
    public boolean pl$usesConfigurablePistonBehavior() {
        return true;
    }

    @Override
    public boolean pl$canMerge(BlockState state, BlockGetter level, BlockPos pos,
                               BlockState mergingIntoState, Direction direction) {
        if (state.getBlock() != mergingIntoState.getBlock()) {
            return false;
        }
        return state.getValues().equals(mergingIntoState.getValues());
    }

    @Override
    public MergeRule pl$getBlockEntityMergeRules() {
        return MergeRule.MERGING;
    }
}
