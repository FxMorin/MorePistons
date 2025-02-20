package ca.fxco.morepistons.mixin.merging;

import ca.fxco.morepistons.MorePistonsConfig;
import ca.fxco.morepistons.blocks.pistons.slabPiston.SlabPistonHeadBlock;
import ca.fxco.pistonlib.api.block.PLBlockBehaviour;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.SlabType;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Optional;

@Mixin(SlabBlock.class)
public class SlabBlockMixin implements PLBlockBehaviour {

    @Override
    public boolean pl$usesConfigurablePistonMerging() {
        return MorePistonsConfig.doSlabMerging;
    }

    @Override
    public boolean pl$canMerge(BlockState state, BlockGetter level, BlockPos pos,
                               BlockState mergingIntoState, Direction direction) {
        if (state.getBlock() != mergingIntoState.getBlock()) {
            return false;
        }
        SlabType type1 = state.getValue(BlockStateProperties.SLAB_TYPE);
        SlabType type2 = mergingIntoState.getValue(BlockStateProperties.SLAB_TYPE);
        if (type1 == type2 || type1 == SlabType.DOUBLE || type2 == SlabType.DOUBLE) {
            return false;
        }
        if (direction == Direction.UP) {
            return type2 != SlabType.TOP && type1 == SlabType.TOP;
        } else if (direction == Direction.DOWN) {
            return type2 != SlabType.BOTTOM && type1 == SlabType.BOTTOM;
        }
        return true;
    }

    @Override
    public BlockState pl$doMerge(BlockState state, BlockGetter level, BlockPos pos,
                                 BlockState mergingIntoState, Direction direction) {
        return mergingIntoState.setValue(BlockStateProperties.SLAB_TYPE, SlabType.DOUBLE);
    }

    @Override
    public boolean pl$canUnMerge(BlockState state, BlockGetter level, BlockPos pos,
                                 BlockState neighborState, Direction direction) {
        if (state.getValue(BlockStateProperties.SLAB_TYPE) != SlabType.DOUBLE) {
            return false;
        }

        Optional<SlabType> neighbourType = neighborState.getOptionalValue(BlockStateProperties.SLAB_TYPE);

        if (neighbourType.isEmpty()) {
            neighbourType = neighborState.getOptionalValue(SlabPistonHeadBlock.SLAB_TYPE);
            if (neighbourType.isEmpty()) {
                return direction.getAxis() == Direction.Axis.Y;
            }
        }

        return neighbourType.get() != SlabType.DOUBLE;
    }

    @Override
    public Pair<BlockState, BlockState> pl$doUnMerge(BlockState state, BlockGetter level,
                                                     BlockPos pos, Direction direction, BlockState pullingState) {
        SlabType firstType;
        SlabType secondType;

        SlabType type = null;
        if (direction.getAxis() != Direction.Axis.Y) {
            type = pullingState.getValue(pullingState.hasProperty(BlockStateProperties.SLAB_TYPE) ?
                    BlockStateProperties.SLAB_TYPE : SlabPistonHeadBlock.SLAB_TYPE);
        }

        if (type == SlabType.BOTTOM || direction == Direction.DOWN) {
            firstType = SlabType.BOTTOM;
            secondType = SlabType.TOP;
        } else {
            firstType = SlabType.TOP;
            secondType = SlabType.BOTTOM;
        }

        return new Pair<>(
                state.setValue(BlockStateProperties.SLAB_TYPE, firstType),
                state.setValue(BlockStateProperties.SLAB_TYPE, secondType)
        );
    }

}
