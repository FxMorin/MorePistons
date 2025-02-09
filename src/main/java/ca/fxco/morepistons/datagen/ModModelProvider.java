package ca.fxco.morepistons.datagen;

import ca.fxco.morepistons.MorePistons;
import ca.fxco.morepistons.base.ModBlocks;
import ca.fxco.morepistons.base.ModItems;
import ca.fxco.morepistons.blocks.pistons.slabPiston.SlabPistonBaseBlock;
import ca.fxco.morepistons.blocks.pistons.slabPiston.SlabPistonHeadBlock;
import ca.fxco.pistonlib.PistonLib;
import ca.fxco.pistonlib.api.PistonLibRegistries;
import ca.fxco.pistonlib.api.pistonLogic.families.PistonFamily;
import ca.fxco.pistonlib.base.ModPistonFamilies;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.blockstates.*;
import net.minecraft.client.data.models.model.*;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.PistonType;
import net.minecraft.world.level.block.state.properties.SlabType;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Optional;

import static ca.fxco.pistonlib.datagen.ModModelProvider.*;
import static net.minecraft.client.data.models.BlockModelGenerators.createSimpleBlock;
import static net.minecraft.client.data.models.BlockModelGenerators.createSlab;

public class ModModelProvider extends FabricModelProvider {

    public static final Logger LOGGER = MorePistons.LOGGER;

	public static final ModelTemplate TEMPLATE_HALF_BLOCK = new ModelTemplate(Optional.of(MorePistons.id("block/template_half_block")), Optional.empty(), TextureSlot.TOP, TextureSlot.SIDE);

	public ModModelProvider(FabricDataOutput dataOutput) {
		super(dataOutput);
	}

	@Override
	public void generateBlockStateModels(BlockModelGenerators generator) {
		LOGGER.info("Generating blockstate definitions and models...");

		for (var entry : PistonLibRegistries.PISTON_FAMILY.entrySet()) {
			ResourceKey<PistonFamily> key = entry.getKey();
			PistonFamily family = entry.getValue();

			if (family == ModPistonFamilies.VANILLA) {
				continue;
			}

			if (family == ca.fxco.morepistons.base.ModPistonFamilies.SLAB) {
				continue;
			}

			LOGGER.info("Generating blockstate definitions and models for piston family " + key.location() + "...");

			registerPistonFamily(generator, family);
		}

		LOGGER.info("Finished generating blockstate definitions and models for pistons, generating for other blocks...");

		registerHalfBlock(generator, ModBlocks.HALF_OBSIDIAN_BLOCK, Blocks.OBSIDIAN);
		registerHalfBlock(generator, ModBlocks.HALF_REDSTONE_BLOCK, Blocks.REDSTONE_BLOCK);
		registerBlockWithCustomModel(generator, ModBlocks.HALF_HONEY_BLOCK);
		registerBlockWithCustomModel(generator, ModBlocks.HALF_SLIME_BLOCK);

		registerBlockWithCustomStates(generator, ModBlocks.HALF_REDSTONE_LAMP_BLOCK,
				createLitFacingBlockState(
						ModelLocationUtils.getModelLocation(ModBlocks.HALF_REDSTONE_LAMP_BLOCK),
						ModelLocationUtils.getModelLocation(ModBlocks.HALF_REDSTONE_LAMP_BLOCK, "_on")));
		registerHalfBlockTextureMap(generator, ModBlocks.HALF_REDSTONE_LAMP_BLOCK, ModelLocationUtils.getModelLocation(Blocks.REDSTONE_LAMP));
		registerHalfBlockTextureMap(generator, ModBlocks.HALF_REDSTONE_LAMP_BLOCK, ModelLocationUtils.getModelLocation(Blocks.REDSTONE_LAMP, "_on"), "_on");

		generator.createRotatedPillarWithHorizontalVariant(ModBlocks.AXIS_LOCKED_BLOCK, TexturedModel.COLUMN_ALT, TexturedModel.COLUMN_HORIZONTAL_ALT);
		generator.createTrivialCube(ModBlocks.DRAG_BLOCK);
		generator.createTrivialCube(ModBlocks.STICKYLESS_BLOCK);
		generator.createTrivialCube(ModBlocks.GLUE_BLOCK);
		generator.createTrivialCube(ModBlocks.MOVE_COUNTING_BLOCK);
		generator.createTrivialCube(ModBlocks.WEAK_REDSTONE_BLOCK);
		generator.createTrivialCube(ModBlocks.AUTO_CRAFTING_BLOCK);
		generator.createTrivialCube(ModBlocks.QUASI_BLOCK);
		generator.createTrivialCube(ModBlocks.ERASE_BLOCK);
		generator.createTrivialCube(ModBlocks.HEAVY_BLOCK);

		registerSlab(generator, Blocks.OBSIDIAN, ModBlocks.OBSIDIAN_SLAB_BLOCK);
		registerStair(generator, Blocks.OBSIDIAN, ModBlocks.OBSIDIAN_STAIR_BLOCK);

		createTrivialBlock(ModBlocks.STICKY_TOP_BLOCK, new TextureMapping().put(TextureSlot.SIDE, TextureMapping.getBlockTexture(Blocks.DEEPSLATE_BRICKS)).put(TextureSlot.TOP, TextureMapping.getBlockTexture(ModBlocks.STICKY_TOP_BLOCK)), ModelTemplates.CUBE_TOP, generator);

		generator.blockStateOutput.accept(createSimpleBlock(ModBlocks.SLIMY_REDSTONE_BLOCK, ModelLocationUtils.getModelLocation(ModBlocks.SLIMY_REDSTONE_BLOCK)));

		registerPoweredBlock(generator, ModBlocks.ALL_SIDED_OBSERVER);

		generator.blockStateOutput.accept(MultiVariantGenerator.multiVariant(ModBlocks.POWERED_STICKY_BLOCK).with(
				PropertyDispatch.property(BlockStateProperties.POWERED)
						.select(false, Variant.variant().with(
								VariantProperties.MODEL, ModelLocationUtils.getModelLocation(ModBlocks.POWERED_STICKY_BLOCK)))
						.select(true, Variant.variant().with(
								VariantProperties.MODEL, ModelLocationUtils.getModelLocation(ModBlocks.POWERED_STICKY_BLOCK, "_on")))
		).with(BlockModelGenerators.createFacingDispatch()));

		generator.blockStateOutput.accept(MultiVariantGenerator.multiVariant(ModBlocks.STICKY_CHAIN_BLOCK, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(ModBlocks.STICKY_CHAIN_BLOCK))).with(BlockModelGenerators.createRotatedPillar()));

		ResourceLocation pistonParticleModel = TEMPLATE_PARTICLE_ONLY.create(ModBlocks.SLAB_MOVING_BLOCK,
				new TextureMapping().put(TextureSlot.PARTICLE,
						TextureMapping.getBlockTexture(Blocks.PISTON, "_side")),
				generator.modelOutput);
		generator.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(
				ModBlocks.SLAB_MOVING_BLOCK, pistonParticleModel));

		ResourceLocation modelLocation = ModelLocationUtils.getModelLocation(ModBlocks.SLAB_PISTON);
		MultiPartGenerator multiPartGenerator =
				MultiPartGenerator.multiPart(ModBlocks.SLAB_PISTON)
						.with(Variant.variant()
								.with(VariantProperties.MODEL, pistonParticleModel));
		createForAllHorizontalFaces(multiPartGenerator,
				modelLocation,
				Condition.condition().term(BlockStateProperties.SLAB_TYPE, SlabType.BOTTOM, SlabType.DOUBLE),
				Condition.condition().term(BlockStateProperties.EXTENDED, false)
		);
		createForAllHorizontalFaces(
				multiPartGenerator,
				modelLocation.withSuffix("_top"),
				SlabPistonBaseBlock.FACING_TOP,
				Condition.condition().term(BlockStateProperties.SLAB_TYPE, SlabType.TOP, SlabType.DOUBLE),
				Condition.condition().term(SlabPistonBaseBlock.EXTENDED_TOP, false)
		);
		createForAllHorizontalFaces(multiPartGenerator,
				modelLocation.withSuffix("_extended"),
				Condition.condition().term(BlockStateProperties.SLAB_TYPE, SlabType.BOTTOM, SlabType.DOUBLE),
				Condition.condition().term(BlockStateProperties.EXTENDED, true)
		);
		createForAllHorizontalFaces(
				multiPartGenerator,
				modelLocation.withSuffix("_extended_top"),
				SlabPistonBaseBlock.FACING_TOP,
				Condition.condition().term(BlockStateProperties.SLAB_TYPE, SlabType.TOP, SlabType.DOUBLE),
				Condition.condition().term(SlabPistonBaseBlock.EXTENDED_TOP, true)
		);
		generator.blockStateOutput.accept(multiPartGenerator);

		modelLocation = ModelLocationUtils.getModelLocation(ModBlocks.SLAB_PISTON);
		multiPartGenerator =
				MultiPartGenerator.multiPart(ModBlocks.SLAB_STICKY_PISTON)
						.with(Variant.variant()
								.with(VariantProperties.MODEL, pistonParticleModel));
		createForAllHorizontalFaces(multiPartGenerator,
				modelLocation.withSuffix("_extended"),
				Condition.condition().term(BlockStateProperties.SLAB_TYPE, SlabType.BOTTOM, SlabType.DOUBLE),
				Condition.condition().term(BlockStateProperties.EXTENDED, true)
		);
		createForAllHorizontalFaces(
				multiPartGenerator,
				modelLocation.withSuffix("_extended_top"),
				SlabPistonBaseBlock.FACING_TOP,
				Condition.condition().term(BlockStateProperties.SLAB_TYPE, SlabType.TOP, SlabType.DOUBLE),
				Condition.condition().term(BlockStateProperties.EXTENDED, true)
		);
		modelLocation = modelLocation.withPath(modelLocation.getPath().replace("slab", "sticky_slab"));
		createForAllHorizontalFaces(multiPartGenerator,
				modelLocation,
				Condition.condition().term(BlockStateProperties.SLAB_TYPE, SlabType.BOTTOM, SlabType.DOUBLE),
				Condition.condition().term(BlockStateProperties.EXTENDED, false)
		);
		createForAllHorizontalFaces(
				multiPartGenerator,
				modelLocation.withSuffix("_top"),
				SlabPistonBaseBlock.FACING_TOP,
				Condition.condition().term(BlockStateProperties.SLAB_TYPE, SlabType.TOP, SlabType.DOUBLE),
				Condition.condition().term(BlockStateProperties.EXTENDED, false)
		);
		generator.blockStateOutput.accept(multiPartGenerator);
		generator.registerSimpleItemModel(ModBlocks.SLAB_STICKY_PISTON, modelLocation);

		modelLocation = ModelLocationUtils.getModelLocation(ModBlocks.SLAB_PISTON_HEAD_BLOCK);
		multiPartGenerator = MultiPartGenerator.multiPart(ModBlocks.SLAB_PISTON_HEAD_BLOCK)
				.with(Variant.variant().with(VariantProperties.MODEL, pistonParticleModel));

		createHeadForAllHorizontalFaces(
				multiPartGenerator,
				modelLocation,
				Condition.condition().term(SlabPistonHeadBlock.SLAB_TYPE, SlabType.BOTTOM, SlabType.DOUBLE),
				Condition.condition().term(SlabPistonHeadBlock.TYPE, PistonType.DEFAULT)
		);
		createHeadForAllHorizontalFaces(
				multiPartGenerator,
				modelLocation.withSuffix("_top"),
				Condition.condition().term(SlabPistonHeadBlock.SLAB_TYPE, SlabType.TOP, SlabType.DOUBLE),
				Condition.condition().term(SlabPistonHeadBlock.TYPE, PistonType.DEFAULT)
		);
		modelLocation = modelLocation.withPath(modelLocation.getPath().replace("slab", "sticky_slab"));
		createHeadForAllHorizontalFaces(
				multiPartGenerator,
				modelLocation,
				Condition.condition().term(SlabPistonHeadBlock.SLAB_TYPE, SlabType.BOTTOM, SlabType.DOUBLE),
				Condition.condition().term(SlabPistonHeadBlock.TYPE, PistonType.STICKY)
		);
		createHeadForAllHorizontalFaces(
				multiPartGenerator,
				modelLocation.withSuffix("_top"),
				Condition.condition().term(SlabPistonHeadBlock.SLAB_TYPE, SlabType.TOP, SlabType.DOUBLE),
				Condition.condition().term(SlabPistonHeadBlock.TYPE, PistonType.STICKY)
		);
		generator.blockStateOutput.accept(multiPartGenerator);

		LOGGER.info("Finished generating blockstate definitions and models!");
	}

	public static void createHeadForAllHorizontalFaces(MultiPartGenerator generator, ResourceLocation resourceLocation,
													   Condition slabCondition, Condition stickyCondition) {
		createForAllHorizontalFaces(generator, resourceLocation, slabCondition, stickyCondition,
				Condition.condition().term(SlabPistonHeadBlock.SHORT, false));
		createForAllHorizontalFaces(generator, resourceLocation.withSuffix("_short"), slabCondition,
				stickyCondition, Condition.condition().term(SlabPistonHeadBlock.SHORT, true));
	}

	public static void createForAllHorizontalFaces(MultiPartGenerator generator,
												   ResourceLocation resourceLocation, Condition... condition) {
		createForAllHorizontalFaces(generator, resourceLocation, BlockStateProperties.FACING, condition);
	}

	public static void createForAllHorizontalFaces(MultiPartGenerator generator, ResourceLocation resourceLocation,
												   EnumProperty<Direction> property, Condition... condition) {
		generator.with(
						Condition.and(
								Condition.condition().term(property, Direction.NORTH),
								Condition.and(condition)
						),
						Variant.variant()
								.with(VariantProperties.MODEL, resourceLocation)
				)
				.with(
						Condition.and(
								Condition.condition().term(property, Direction.EAST),
								Condition.and(condition)
						),
						Variant.variant()
								.with(VariantProperties.MODEL, resourceLocation)
								.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
				)
				.with(
						Condition.and(
								Condition.condition().term(property, Direction.SOUTH),
								Condition.and(condition)
						),
						Variant.variant()
								.with(VariantProperties.MODEL, resourceLocation)
								.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
				)
				.with(
						Condition.and(
								Condition.condition().term(property, Direction.WEST),
								Condition.and(condition)
						),
						Variant.variant()
								.with(VariantProperties.MODEL, resourceLocation)
								.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
				);
	}

	@Override
	public void generateItemModels(ItemModelGenerators generator) {
		generator.generateFlatItem(ModItems.PISTON_WAND, ModelTemplates.FLAT_ITEM);
		generator.generateFlatItem(ModItems.PISTON_DEBUG_WAND, ModelTemplates.FLAT_ITEM);
		generator.generateFlatItem(ModBlocks.STICKY_CHAIN_BLOCK.asItem(), ModelTemplates.FLAT_ITEM);
	}

	private static void registerSlab(BlockModelGenerators generator, Block baseBlock, Block block) {
		TextureMapping textureBase = TextureMapping.cube(baseBlock);
		ResourceLocation bottom = ModelTemplates.SLAB_BOTTOM.create(block, textureBase, generator.modelOutput);
		ResourceLocation top = ModelTemplates.SLAB_TOP.create(block, textureBase, generator.modelOutput);
		ResourceLocation _double = ModelTemplates.CUBE_COLUMN.createWithOverride(block, "_double", textureBase, generator.modelOutput);
		generator.blockStateOutput.accept(createSlab(block, bottom, top, _double));
		generator.registerSimpleItemModel(block, bottom);
	}

	private static void registerStair(BlockModelGenerators generator, Block baseBlock, Block block) {
		TextureMapping textureBase = TextureMapping.cube(baseBlock);
		ResourceLocation inner = ModelTemplates.STAIRS_INNER.create(block, textureBase, generator.modelOutput);
		ResourceLocation flat = ModelTemplates.STAIRS_STRAIGHT.create(block, textureBase, generator.modelOutput);
		ResourceLocation outer = ModelTemplates.STAIRS_OUTER.create(block, textureBase, generator.modelOutput);
		generator.blockStateOutput.accept(BlockModelGenerators.createStairs(block, inner, flat, outer));
		generator.registerSimpleItemModel(block, flat);
	}

	public static void registerCubeTextureMap(BlockModelGenerators generator, Block block,
											  ResourceLocation baseTexture, @Nullable String suffix) {
		TextureMapping halfBlockTextureMap = new TextureMapping().put(TextureSlot.ALL, baseTexture);
		if (suffix == null) {
			ModelTemplates.CUBE_ALL.create(block, halfBlockTextureMap, generator.modelOutput);
		} else {
			ModelTemplates.CUBE_ALL.createWithSuffix(block, suffix, halfBlockTextureMap, generator.modelOutput);
		}
	}

	public static void registerHalfBlockTextureMap(BlockModelGenerators generator, Block halfBlock, ResourceLocation baseTexture) {
		registerHalfBlockTextureMap(generator, halfBlock, baseTexture, null);
	}

	public static void registerHalfBlockTextureMap(BlockModelGenerators generator, Block halfBlock, ResourceLocation baseTexture, @Nullable String suffix) {
		TextureMapping halfBlockTextureMap = new TextureMapping().put(TextureSlot.SIDE, baseTexture).put(TextureSlot.TOP, baseTexture);
		if (suffix == null) {
			TEMPLATE_HALF_BLOCK.create(halfBlock, halfBlockTextureMap, generator.modelOutput);
		} else {
			TEMPLATE_HALF_BLOCK.createWithSuffix(halfBlock, suffix, halfBlockTextureMap, generator.modelOutput);
		}
	}

	public static void registerBlockWithCustomModel(BlockModelGenerators generator, Block halfBlock) {
		registerHalfBlock(generator, halfBlock, null);
	}

	public static void registerBlockWithCustomStates(BlockModelGenerators generator, Block halfBlock, PropertyDispatch customStates) {
		generator.blockStateOutput.accept(MultiVariantGenerator.multiVariant(halfBlock, Variant.variant()
				.with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(halfBlock))).with(customStates)
		);
	}

	public static void registerHalfBlock(BlockModelGenerators generator, Block halfBlock, @Nullable Block base) {
		generator.blockStateOutput.accept(MultiVariantGenerator.multiVariant(
				halfBlock,
				Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(halfBlock))
		).with(generator.createColumnWithFacing()));

		if (base != null) {
			ResourceLocation baseTextureId = TextureMapping.getBlockTexture(base);

			TextureMapping halfBlockTextureMap = new TextureMapping()
					.put(TextureSlot.SIDE, baseTextureId)
					.put(TextureSlot.TOP, baseTextureId);

			TEMPLATE_HALF_BLOCK.create(halfBlock, halfBlockTextureMap, generator.modelOutput);
		}
	}

	public static void registerPoweredBlock(BlockModelGenerators generator, Block block) {
		ResourceLocation powerOff = ModelLocationUtils.getModelLocation(block);
		ResourceLocation powerOn = ModelLocationUtils.getModelLocation(block, "_on");
		registerBlockWithCustomStates(generator, block,
				PropertyDispatch.property(BlockStateProperties.POWERED)
						.select(false, Variant.variant().with(VariantProperties.MODEL, powerOff))
						.select(true, Variant.variant().with(VariantProperties.MODEL, powerOn)));
		registerCubeTextureMap(generator, block, powerOff, null);
		registerCubeTextureMap(generator, block, powerOn, "_on");
	}

	public static PropertyDispatch createLitFacingBlockState(ResourceLocation offModelId, ResourceLocation onModelId) {
		return PropertyDispatch
				.properties(BlockStateProperties.FACING, BlockStateProperties.LIT)
				.select(Direction.NORTH, false,
						Variant.variant()
								.with(VariantProperties.MODEL, offModelId)
								.with(VariantProperties.X_ROT, VariantProperties.Rotation.R90))
				.select(Direction.SOUTH, false,
						Variant.variant()
								.with(VariantProperties.MODEL, offModelId)
								.with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
								.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
				.select(Direction.EAST, false,
						Variant.variant()
								.with(VariantProperties.MODEL, offModelId)
								.with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
								.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))
				.select(Direction.WEST, false,
						Variant.variant()
								.with(VariantProperties.MODEL, offModelId)
								.with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
								.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270))
				.select(Direction.DOWN, false,
						Variant.variant()
								.with(VariantProperties.MODEL, offModelId)
								.with(VariantProperties.X_ROT, VariantProperties.Rotation.R180))
				.select(Direction.UP, false,
						Variant.variant()
								.with(VariantProperties.MODEL, offModelId))
				.select(Direction.NORTH, true,
						Variant.variant()
								.with(VariantProperties.MODEL, onModelId)
								.with(VariantProperties.X_ROT, VariantProperties.Rotation.R90))
				.select(Direction.SOUTH, true,
						Variant.variant()
								.with(VariantProperties.MODEL, onModelId)
								.with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
								.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
				.select(Direction.EAST, true,
						Variant.variant()
								.with(VariantProperties.MODEL, onModelId)
								.with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
								.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))
				.select(Direction.WEST, true,
						Variant.variant()
								.with(VariantProperties.MODEL, onModelId)
								.with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
								.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270))
				.select(Direction.DOWN, true,
						Variant.variant()
								.with(VariantProperties.MODEL, onModelId)
								.with(VariantProperties.X_ROT, VariantProperties.Rotation.R180))
				.select(Direction.UP, true,
						Variant.variant()
								.with(VariantProperties.MODEL, onModelId));
	}
}
