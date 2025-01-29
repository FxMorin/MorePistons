package ca.fxco.morepistons.base;

import ca.fxco.morepistons.blocks.pistons.configurablePiston.ConfigurableMovingBlockEntity;
import ca.fxco.morepistons.blocks.pistons.fastPiston.FastMovingBlockEntity;
import ca.fxco.morepistons.blocks.pistons.speedPiston.SpeedMovingBlockEntity;
import ca.fxco.pistonlib.api.pistonLogic.families.PistonFamilies;
import ca.fxco.pistonlib.api.pistonLogic.families.PistonFamily;
import ca.fxco.pistonlib.blocks.pistons.basePiston.BasicMovingBlockEntity;
import ca.fxco.pistonlib.blocks.pistons.movableBlockEntities.MBEMovingBlockEntity;
import ca.fxco.pistonlib.pistonLogic.families.PistonBehaviorImpl;
import ca.fxco.pistonlib.pistonLogic.families.PistonFamilyImpl;
import net.minecraft.world.level.block.state.properties.PistonType;

import static ca.fxco.morepistons.MorePistons.id;

public class ModPistonFamilies {

    public static final PistonFamily BASIC = register("basic", PistonFamilyImpl.builder()
            .behavior(PistonBehaviorImpl.DEFAULT)
            .base(PistonType.DEFAULT, ModBlocks.BASIC_PISTON)
            .base(PistonType.STICKY, ModBlocks.BASIC_STICKY_PISTON)
            .head(ModBlocks.BASIC_PISTON_HEAD)
            .moving(ModBlocks.BASIC_MOVING_BLOCK)
            .movingBlockEntity(ModBlockEntities.BASIC_MOVING_BLOCK_ENTITY, BasicMovingBlockEntity::new));

    public static final PistonFamily LONG = register("long", PistonFamilyImpl.builder()
            .behavior(PistonBehaviorImpl.builder()
                    .maxLength(12)
                    .noQuasi())
            .base(PistonType.DEFAULT, ModBlocks.LONG_PISTON)
            .base(PistonType.STICKY, ModBlocks.LONG_STICKY_PISTON)
            .arm(ModBlocks.LONG_PISTON_ARM)
            .head(ModBlocks.LONG_PISTON_HEAD)
            .moving(ModBlocks.LONG_MOVING_BLOCK)
            .movingBlockEntity(ModBlockEntities.BASIC_MOVING_BLOCK_ENTITY, BasicMovingBlockEntity::new));

    public static final PistonFamily CONFIGURABLE = register("configurable", PistonFamilyImpl.builder()
            .behavior(PistonBehaviorImpl.builder()
                    .maxLength(2)
                    .noQuasi()
                    .extendingSpeed(0.1F)
                    .retractingSpeed(0.5F))
            .base(PistonType.DEFAULT, ModBlocks.CONFIGURABLE_PISTON)
            .base(PistonType.STICKY, ModBlocks.CONFIGURABLE_STICKY_PISTON)
            .arm(ModBlocks.CONFIGURABLE_PISTON_ARM)
            .head(ModBlocks.CONFIGURABLE_PISTON_HEAD)
            .moving(ModBlocks.CONFIGURABLE_MOVING_BLOCK)
            .movingBlockEntity(ModBlockEntities.CONFIGURABLE_MOVING_BLOCK_ENTITY, ConfigurableMovingBlockEntity::new));

    public static final PistonFamily STALE = register("stale", PistonFamilyImpl.builder()
            .behavior(PistonBehaviorImpl.builder()
                    .noQuasi())
            .base(PistonType.DEFAULT, ModBlocks.STALE_PISTON)
            .base(PistonType.STICKY, ModBlocks.STALE_STICKY_PISTON)
            .head(ModBlocks.STALE_PISTON_HEAD)
            .moving(ModBlocks.STALE_MOVING_BLOCK)
            .movingBlockEntity(ModBlockEntities.BASIC_MOVING_BLOCK_ENTITY, BasicMovingBlockEntity::new));

    public static final PistonFamily VERY_QUASI = register("very_quasi", PistonFamilyImpl.builder()
            .behavior(PistonBehaviorImpl.DEFAULT)
            .base(PistonType.DEFAULT, ModBlocks.VERY_QUASI_PISTON)
            .base(PistonType.STICKY, ModBlocks.VERY_QUASI_STICKY_PISTON)
            .head(ModBlocks.VERY_QUASI_PISTON_HEAD)
            .moving(ModBlocks.VERY_QUASI_MOVING_BLOCK)
            .movingBlockEntity(ModBlockEntities.BASIC_MOVING_BLOCK_ENTITY, BasicMovingBlockEntity::new));

    public static final PistonFamily STRONG = register("strong", PistonFamilyImpl.builder()
            .behavior(PistonBehaviorImpl.builder()
                    .speed(0.05F)
                    .pushLimit(24))
            .base(PistonType.DEFAULT, ModBlocks.STRONG_PISTON)
            .base(PistonType.STICKY, ModBlocks.STRONG_STICKY_PISTON)
            .head(ModBlocks.STRONG_PISTON_HEAD)
            .moving(ModBlocks.STRONG_MOVING_BLOCK)
            .movingBlockEntity(ModBlockEntities.SPEED_MOVING_BLOCK_ENTITY, SpeedMovingBlockEntity::new)
            .customTextures(true));

    public static final PistonFamily FAST = register("fast", PistonFamilyImpl.builder()
            .behavior(PistonBehaviorImpl.builder()
                    .pushLimit(2))
            .base(PistonType.DEFAULT, ModBlocks.FAST_PISTON)
            .base(PistonType.STICKY, ModBlocks.FAST_STICKY_PISTON)
            .head(ModBlocks.FAST_PISTON_HEAD)
            .moving(ModBlocks.FAST_MOVING_BLOCK)
            .movingBlockEntity(ModBlockEntities.FAST_MOVING_BLOCK_ENTITY, FastMovingBlockEntity::new));

    public static final PistonFamily FRONT_POWERED = register("front_powered", PistonFamilyImpl.builder()
            .behavior(PistonBehaviorImpl.builder()
                    .frontPowered())
            .base(PistonType.DEFAULT, ModBlocks.FRONT_POWERED_PISTON)
            .base(PistonType.STICKY, ModBlocks.FRONT_POWERED_STICKY_PISTON)
            .head(ModBlocks.FRONT_POWERED_PISTON_HEAD)
            .moving(ModBlocks.FRONT_POWERED_MOVING_BLOCK)
            .movingBlockEntity(ModBlockEntities.BASIC_MOVING_BLOCK_ENTITY, BasicMovingBlockEntity::new));

    public static final PistonFamily SUPER = register("super", PistonFamilyImpl.builder()
            .behavior(PistonBehaviorImpl.builder()
                    .pushLimit(Integer.MAX_VALUE)
                    .verySticky())
            .base(PistonType.DEFAULT, ModBlocks.SUPER_PISTON)
            .base(PistonType.STICKY, ModBlocks.SUPER_STICKY_PISTON)
            .head(ModBlocks.SUPER_PISTON_HEAD)
            .moving(ModBlocks.SUPER_MOVING_BLOCK)
            .movingBlockEntity(ModBlockEntities.BASIC_MOVING_BLOCK_ENTITY, BasicMovingBlockEntity::new));

    public static final PistonFamily MBE = register("mbe", PistonFamilyImpl.builder()
            .behavior(PistonBehaviorImpl.DEFAULT)
            .base(PistonType.DEFAULT, ModBlocks.MBE_PISTON)
            .base(PistonType.STICKY, ModBlocks.MBE_STICKY_PISTON)
            .head(ModBlocks.MBE_PISTON_HEAD_BLOCK)
            .moving(ModBlocks.MBE_MOVING_BLOCK)
            .movingBlockEntity(ModBlockEntities.MBE_MOVING_BLOCK_ENTITY, MBEMovingBlockEntity::new));

    public static final PistonFamily VERY_STICKY = register("very_sticky", PistonFamilyImpl.builder()
            .behavior(PistonBehaviorImpl.builder()
                    .verySticky())
            .base(PistonType.STICKY, ModBlocks.VERY_STICKY_PISTON)
            .head(ModBlocks.STICKY_PISTON_HEAD)
            .moving(ModBlocks.STICKY_MOVING_BLOCK)
            .movingBlockEntity(ModBlockEntities.BASIC_MOVING_BLOCK_ENTITY, BasicMovingBlockEntity::new));

    private static PistonFamily register(String name, PistonFamily.Builder familyBuilder) {
        return register(name, familyBuilder.build());
    }

    private static PistonFamily register(String name, PistonFamily family) {
        return PistonFamilies.register(id(name), family);
    }

    public static void bootstrap() {}
}
