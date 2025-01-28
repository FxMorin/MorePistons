package ca.fxco.morepistons.datagen;

import ca.fxco.morepistons.base.ModBlocks;
import ca.fxco.morepistons.base.ModItems;
import ca.fxco.pistonlib.PistonLib;
import ca.fxco.pistonlib.api.PistonLibRegistries;
import ca.fxco.pistonlib.api.pistonLogic.families.PistonFamily;
import ca.fxco.pistonlib.base.ModPistonFamilies;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.blockstates.Variant;
import net.minecraft.client.data.models.blockstates.VariantProperties;
import net.minecraft.client.data.models.model.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.slf4j.Logger;

import static ca.fxco.pistonlib.datagen.ModModelProvider.*;
import static net.minecraft.client.data.models.BlockModelGenerators.createSimpleBlock;
import static net.minecraft.client.data.models.BlockModelGenerators.createSlab;

public class ModModelProvider extends FabricModelProvider {

    public static final Logger LOGGER = PistonLib.LOGGER;

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

            LOGGER.info("Generating blockstate definitions and models for piston family " + key.location()+"...");

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
        generator.createTrivialCube(ModBlocks.SLIPPERY_REDSTONE_BLOCK);
        generator.createTrivialCube(ModBlocks.SLIPPERY_STONE_BLOCK);
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
		generator.blockStateOutput.accept(createSimpleBlock(ModBlocks.SLIPPERY_SLIME_BLOCK, ModelLocationUtils.getModelLocation(ModBlocks.SLIPPERY_SLIME_BLOCK)));

		registerPoweredBlock(generator, ModBlocks.ALL_SIDED_OBSERVER);

		generator.blockStateOutput.accept(MultiVariantGenerator.multiVariant(ModBlocks.POWERED_STICKY_BLOCK).with(
				PropertyDispatch.property(BlockStateProperties.POWERED)
						.select(false, Variant.variant().with(
								VariantProperties.MODEL, ModelLocationUtils.getModelLocation(ModBlocks.POWERED_STICKY_BLOCK)))
						.select(true, Variant.variant().with(
								VariantProperties.MODEL, ModelLocationUtils.getModelLocation(ModBlocks.POWERED_STICKY_BLOCK, "_on")))
		).with(BlockModelGenerators.createFacingDispatch()));

		generator.blockStateOutput.accept(MultiVariantGenerator.multiVariant(ModBlocks.STICKY_CHAIN_BLOCK, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(ModBlocks.STICKY_CHAIN_BLOCK))).with(BlockModelGenerators.createRotatedPillar()));

		LOGGER.info("Finished generating blockstate definitions and models!");
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
}
