package ca.fxco.morepistons.blocks.pistons.slabPiston;

import ca.fxco.pistonlib.blocks.pistons.basePiston.BasicPistonHeadBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SlabPistonHeadBlock extends BasicPistonHeadBlock {
    public static final EnumProperty<SlabType> SLAB_TYPE = EnumProperty.create("slab_type", SlabType .class);
    protected static final VoxelShape[] BOTTOM_SHAPES = new VoxelShape[4];
    protected static final VoxelShape[] TOP_SHAPES = new VoxelShape[4];
    protected static final VoxelShape[] SHORT_BOTTOM_SHAPES = new VoxelShape[4];
    protected static final VoxelShape[] SHORT_TOP_SHAPES = new VoxelShape[4];

    public SlabPistonHeadBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, TYPE, SHORT, SLAB_TYPE);
    }

    @Override
    public boolean isFittingBase(BlockState headState, BlockState behindState) {
        BooleanProperty extended;
        EnumProperty<Direction> direction;
        if (headState.getValue(SLAB_TYPE) == SlabType.TOP) {
            extended = SlabPistonBaseBlock.EXTENDED_TOP;
            direction = SlabPistonBaseBlock.FACING_TOP;
        } else {
            extended = BlockStateProperties.EXTENDED;
            direction = SlabPistonBaseBlock.FACING;
        }
        return behindState.is(this.getFamily().getBase(headState.getValue(TYPE))) &&
                behindState.getValue(extended) &&
                behindState.getValue(direction) == headState.getValue(FACING);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction facing = state.getValue(FACING);
        if (facing.ordinal() < 2) {
            return Shapes.empty();
        }

        return switch (state.getValue(SLAB_TYPE)) {
            case DOUBLE -> Shapes.or(BOTTOM_SHAPES[facing.ordinal() - 2], TOP_SHAPES[facing.ordinal() - 2]);
            case TOP -> TOP_SHAPES[facing.ordinal() - 2];
            default -> BOTTOM_SHAPES[facing.ordinal() - 2];
        };
    }

    static {
        BOTTOM_SHAPES[0] = Shapes.or(box(0, 0, 0, 16, 8, 4), box(6, 3, 4, 10, 5, 20));
        BOTTOM_SHAPES[1] = Shapes.or(box(0, 0, 12, 16, 8, 16), box(6, 3, -4, 10, 5, 12));
        BOTTOM_SHAPES[2] = Shapes.or(box(0, 0, 0, 4, 8, 16), box(4, 3, 6, 20, 5, 10));
        BOTTOM_SHAPES[3] = Shapes.or(box(12, 0, 0, 16, 8, 16), box(-4, 3, 6, 12, 5, 10));

        TOP_SHAPES[0] = Shapes.or(box(0, 8, 0, 16, 16, 4), box(6, 11, 4, 10, 13, 20));
        TOP_SHAPES[1] = Shapes.or(box(0, 8, 12, 16, 16, 16), box(6, 11, -4, 10, 13, 12));
        TOP_SHAPES[2] = Shapes.or(box(0, 8, 0, 4, 16, 16), box(4, 11, 6, 20, 13, 10));
        TOP_SHAPES[3] = Shapes.or(box(12, 8, 0, 16, 16, 16), box(-4, 11, 6, 12, 13, 10));
    }
}
