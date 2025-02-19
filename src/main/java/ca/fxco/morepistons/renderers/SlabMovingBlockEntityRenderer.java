package ca.fxco.morepistons.renderers;

import ca.fxco.morepistons.base.ModBlockStateProperties;
import ca.fxco.morepistons.blocks.pistons.slabPiston.SlabMovingBlockEntity;
import ca.fxco.morepistons.blocks.pistons.slabPiston.SlabPistonBaseBlock;
import ca.fxco.morepistons.blocks.pistons.slabPiston.SlabPistonHeadBlock;
import ca.fxco.pistonlib.api.pistonLogic.families.PistonFamily;
import ca.fxco.pistonlib.blocks.pistons.basePiston.BasicPistonBaseBlock;
import ca.fxco.pistonlib.blocks.pistons.basePiston.BasicPistonHeadBlock;
import ca.fxco.pistonlib.renderers.BasicMovingBlockEntityRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.SlabType;

public class SlabMovingBlockEntityRenderer extends BasicMovingBlockEntityRenderer<SlabMovingBlockEntity> {

    public SlabMovingBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(SlabMovingBlockEntity mbe, float partialTick, PoseStack stack, MultiBufferSource bufferSource,
                       int light, int overlay) {
        Level level = mbe.getLevel();
        if (level == null) {
            return;
        }

        BlockState state = mbe.getMovedState();

        if (state.isAir()) {
            return;
        }

        ModelBlockRenderer.enableCaching();
        stack.pushPose();
        stack.translate(mbe.getXOff(partialTick), mbe.getYOff(partialTick), mbe.getZOff(partialTick));

        Direction moveDir = mbe.getMovementDirection();
        BlockPos toPos = mbe.getBlockPos();
        BlockPos fromPos = toPos.relative(moveDir.getOpposite());

        if (mbe.isSourcePiston()) {
            if (mbe.isExtending() || mbe.extendedSides != SlabType.TOP)
                this.renderMovingSource(mbe, level, fromPos, toPos, partialTick,
                    stack, bufferSource, light, overlay, false);
        } else {
            this.renderMovingBlock(mbe, level, fromPos, toPos, partialTick, stack, bufferSource, light, overlay);
        }

        stack.popPose();
        if (mbe.isSourcePiston() && !mbe.isExtending() && mbe.extendedSides != SlabType.BOTTOM
                && state.getValue(SlabPistonBaseBlock.TYPE) == SlabType.DOUBLE) {
            stack.pushPose();
            Direction pistonDir = state.getValue(ModBlockStateProperties.FACING_TOP);
            stack.translate(mbe.getXOff(partialTick, pistonDir),
                    mbe.getYOff(partialTick, pistonDir), mbe.getZOff(partialTick, pistonDir));

            fromPos = toPos.relative(pistonDir);

            this.renderMovingSource(mbe, level, fromPos, toPos, partialTick,
                    stack, bufferSource, light, overlay, true);


            stack.popPose();
        }
        stack.pushPose();

        if (mbe.isSourcePiston()) {
            this.renderStaticSource(mbe, level, fromPos, toPos, partialTick, stack, bufferSource, light, overlay);
        } else {
            this.renderStaticBlock(mbe, level, fromPos, toPos, partialTick, stack, bufferSource, light, overlay);
        }

        stack.popPose();
        ModelBlockRenderer.clearCache();
    }

    protected void renderMovingSource(SlabMovingBlockEntity mbe, Level level, BlockPos fromPos,
                                      BlockPos toPos, float partialTick, PoseStack stack,
                                      MultiBufferSource bufferSource, int light, int overlay, boolean isSecondArm) {
        BlockState state = mbe.getMovedState();

        if (mbe.isExtending()) {
            if (state.getBlock() instanceof BasicPistonHeadBlock) {
                this.renderBlock(mbe, fromPos, state.setValue(BasicPistonHeadBlock.SHORT,
                        mbe.getProgress(partialTick) <= 0.5F), stack, bufferSource, level, false, overlay);
            }
        } else if (state.getBlock() instanceof BasicPistonBaseBlock base) {
            PistonFamily family = mbe.getFamily();
            Direction facing;
            SlabType armType;
            switch (state.getValue(SlabPistonBaseBlock.TYPE)) {
                case DOUBLE -> {
                    facing = state.getValue(BasicPistonBaseBlock.FACING);
                    Direction topFacing = state.getValue(ModBlockStateProperties.FACING_TOP);
                    if (facing != topFacing) {
                        if (isSecondArm) {
                            armType = SlabType.TOP;
                            facing = topFacing;
                        } else {
                            armType = SlabType.BOTTOM;
                        }
                    } else {
                        armType = SlabType.DOUBLE;
                    }
                }
                case TOP -> {
                    armType = SlabType.TOP;
                    facing = state.getValue(ModBlockStateProperties.FACING_TOP);
                }
                default -> {
                    armType = SlabType.BOTTOM;
                    facing = state.getValue(SlabPistonBaseBlock.FACING);
                }
            }
            ;
            BlockState headState = family.getHead().defaultBlockState()
                    .setValue(BasicPistonHeadBlock.TYPE, base.pl$getPistonController().getType())
                    .setValue(BasicPistonHeadBlock.FACING, facing)
                    .setValue(BasicPistonHeadBlock.SHORT, mbe.getProgress(partialTick) >= 0.5F)
                    .setValue(SlabPistonHeadBlock.SLAB_TYPE, armType);

            this.renderBlock(mbe, fromPos, headState, stack, bufferSource, level, false, overlay);
        } else if (state.getBlock() instanceof BasicPistonHeadBlock) {
            BlockState headState = state
                    .setValue(BasicPistonHeadBlock.SHORT, mbe.getProgress(partialTick) >= 0.5F);
            this.renderBlock(mbe, fromPos, headState, stack, bufferSource, level, false, overlay);
        }
    }

    @Override
    protected void renderStaticSource(SlabMovingBlockEntity mbe, Level level, BlockPos fromPos, BlockPos toPos, float partialTick, PoseStack stack, MultiBufferSource bufferSource, int light, int overlay) {
        if (!mbe.isExtending()) {
            BlockState state = mbe.getMovedState();
            if (state.getBlock() instanceof BasicPistonBaseBlock) {
                this.renderBlock(mbe, fromPos, state
                                .setValue(BlockStateProperties.EXTENDED,
                                        mbe.extendedSides != SlabType.TOP)
                                .setValue(SlabPistonBaseBlock.EXTENDED_TOP,
                                        mbe.extendedSides != SlabType.BOTTOM),
                        stack, bufferSource, level, false, overlay);
            } else if (state.getBlock() instanceof BasicPistonHeadBlock) {
                PistonFamily family = mbe.getFamily();
                Direction facing = state.getValue(BasicPistonHeadBlock.FACING);
                BlockState armState = family.getArm().defaultBlockState().setValue(BasicPistonHeadBlock.FACING, facing)
                        .setValue(BasicPistonHeadBlock.SHORT, false);
                this.renderBlock(mbe, fromPos, armState, stack, bufferSource, level, false, overlay);
            }
        }
    }
}
