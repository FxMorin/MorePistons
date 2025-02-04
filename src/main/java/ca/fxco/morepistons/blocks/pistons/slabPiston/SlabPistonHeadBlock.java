package ca.fxco.morepistons.blocks.pistons.slabPiston;

import ca.fxco.pistonlib.blocks.pistons.basePiston.BasicPistonHeadBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SlabPistonHeadBlock extends BasicPistonHeadBlock {
    public static final EnumProperty<SlabType> SLAB_TYPE = EnumProperty.create("slab_type", SlabType .class);

    public SlabPistonHeadBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, TYPE, SHORT, SLAB_TYPE);
    }

    @Override
    public boolean isFittingBase(BlockState headState, BlockState behindState) {
        return behindState.is(this.getFamily().getBase(headState.getValue(TYPE))) &&
                behindState.getValue(BlockStateProperties.EXTENDED) &&
                behindState.getValue(headState.getValue(SLAB_TYPE) == SlabType.TOP ?
                        SlabPistonBaseBlock.FACING_TOP : FACING) == headState.getValue(FACING);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return super.getShape(state, level, pos, context); //TODO create shape for each state
    }
}
