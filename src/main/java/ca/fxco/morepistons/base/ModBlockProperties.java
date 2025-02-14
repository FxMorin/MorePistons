package ca.fxco.morepistons.base;

import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.SlabType;

public class ModBlockProperties {
    public static final EnumProperty<SlabType> HEAD_SLAB_TYPE = EnumProperty.create("slab_type", SlabType .class);
}
