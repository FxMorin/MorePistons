package ca.fxco.morepistons.base;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.SlabType;

public class ModBlockStateProperties {
    public static final EnumProperty<SlabType> HEAD_SLAB_TYPE = EnumProperty.create("slab_type", SlabType .class);
    public static final BooleanProperty EXTENDED_TOP = BooleanProperty.create("extended_top");
    public static final EnumProperty<Direction> FACING_TOP = EnumProperty.create("facing_top", Direction.class, Direction.Plane.HORIZONTAL);
}
