package ca.fxco.morepistons.blocks.pistons.slabPiston;

import ca.fxco.pistonlib.blocks.pistons.basePiston.BasicPistonHeadBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.SlabType;

public class SlabPistonHeadBlock extends BasicPistonHeadBlock {
    public static final EnumProperty<SlabType> SLAB_TYPE = EnumProperty.create("slab_type", SlabType .class);

    public SlabPistonHeadBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, TYPE, SHORT, SLAB_TYPE);
    }
}
