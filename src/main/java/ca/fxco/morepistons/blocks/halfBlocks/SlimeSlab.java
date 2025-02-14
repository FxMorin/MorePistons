package ca.fxco.morepistons.blocks.halfBlocks;

import ca.fxco.pistonlib.PistonLib;
import ca.fxco.pistonlib.api.pistonLogic.sticky.StickRules;
import ca.fxco.pistonlib.api.pistonLogic.sticky.StickyGroup;
import ca.fxco.pistonlib.api.pistonLogic.sticky.StickyType;
import ca.fxco.pistonlib.base.ModStickyGroups;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static ca.fxco.morepistons.utils.HalfBlockUtils.SIDES_LIST;

public class SlimeSlab extends SlabBlock {

    public static final Map<Direction, StickyType> STICKY_SIDES = new HashMap<>();

    public SlimeSlab(Properties properties) {
        super(properties);
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        if (entity.isSuppressingBounce()) {
            super.fallOn(level, state, pos, entity, fallDistance);
        } else {
            entity.causeFallDamage(fallDistance, 0.0F, level.damageSources().fall());
        }
    }

    @Override
    public void updateEntityMovementAfterFallOn(BlockGetter level, Entity entity) {
        if (entity.isSuppressingBounce()) {
            super.updateEntityMovementAfterFallOn(level, entity);
        } else {
            this.bounce(entity);
        }
    }

    protected void bounce(Entity entity) {
        Vec3 velocity = entity.getDeltaMovement();
        if (velocity.y < 0.0) {
            double bounceStrength = entity instanceof LivingEntity ? 1.0 : 0.8;
            entity.setDeltaMovement(velocity.x, -velocity.y * bounceStrength, velocity.z);
        }
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        double velocityY = Math.abs(entity.getDeltaMovement().y);
        if (velocityY < 0.1 && !(entity.isSteppingCarefully())) {
            double bounceStrength = 0.4 + velocityY * 0.2;
            entity.setDeltaMovement(entity.getDeltaMovement().multiply(bounceStrength, 1.0, bounceStrength));
        }
        super.stepOn(level, pos, state, entity);
    }

    @Override
    public boolean pl$usesConfigurablePistonStickiness() {
        return true;
    }

    @Override
    public @Nullable StickyGroup pl$getStickyGroup(BlockState state) {
        return ModStickyGroups.SLIME;
    }

    @Override
    public Map<Direction, StickyType> pl$stickySides(BlockState state) {
        return switch (state.getValue(TYPE)) {
            case TOP -> SIDES_LIST[Direction.DOWN.ordinal()];
            case BOTTOM -> SIDES_LIST[Direction.UP.ordinal()];
            case DOUBLE -> STICKY_SIDES;
        };
    }

    @Override
    public StickyType pl$sideStickiness(BlockState state, Direction dir) {
        switch (state.getValue(TYPE)) {
            case TOP -> {
                if (dir == Direction.UP) {
                    return StickyType.STICKY;
                } else if (dir == Direction.DOWN) {
                    return StickyType.DEFAULT;
                }
            }
            case BOTTOM -> {
                if (dir == Direction.DOWN) {
                    return StickyType.STICKY;
                } else if (dir == Direction.UP) {
                    return StickyType.DEFAULT;
                }
            }
            case DOUBLE -> {
                return StickyType.STICKY;
            }
        }

        return StickyType.CONDITIONAL;
    }

    // Only the sides call the conditional check
    @Override
    public boolean pl$matchesStickyConditions(BlockState state, BlockState neighborState, Direction dir) {
        SlabType slabType = state.getValue(TYPE);
        if (slabType != SlabType.DOUBLE) {
            Optional<SlabType> neighborSlabType = neighborState.getOptionalValue(TYPE);
            if (neighborSlabType.isPresent()) {
                return neighborSlabType.get() == slabType || neighborSlabType.get() == SlabType.DOUBLE;
            }
        }
        StickyGroup group = neighborState.pl$getStickyGroup();
        if (group != null) {
            return StickRules.test(ModStickyGroups.SLIME, group);
        }
        return true;
    }

    static {
        for (Direction direction : PistonLib.DIRECTIONS) {
            STICKY_SIDES.put(direction, StickyType.STICKY);
        }
    }

}
