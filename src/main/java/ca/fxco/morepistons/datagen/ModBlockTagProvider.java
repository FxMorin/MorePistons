package ca.fxco.morepistons.datagen;

import java.util.concurrent.CompletableFuture;

import ca.fxco.morepistons.MorePistons;
import ca.fxco.pistonlib.api.PistonLibRegistries;
import org.slf4j.Logger;

import ca.fxco.morepistons.base.ModBlocks;
import ca.fxco.pistonlib.base.ModTags;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;

import net.minecraft.core.HolderLookup;

public class ModBlockTagProvider extends FabricTagProvider.BlockTagProvider {

    public static final Logger LOGGER = MorePistons.LOGGER;

	public ModBlockTagProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
		super(output, registriesFuture);
	}

	@Override
	protected void addTags(HolderLookup.Provider registries) {
		LOGGER.info("Generating block tags...");

		FabricTagBuilder pistonsTag = getOrCreateTagBuilder(ModTags.PISTONS);
		FabricTagBuilder movingPistonsTag = getOrCreateTagBuilder(ModTags.MOVING_PISTONS);

		PistonLibRegistries.PISTON_FAMILY.forEach(family -> {
		    family.getBases().forEach((type, base) -> pistonsTag.add(base));
		    movingPistonsTag.add(family.getMoving());
		});

		getOrCreateTagBuilder(ModTags.UNPUSHABLE).add(ModBlocks.OBSIDIAN_SLAB_BLOCK, ModBlocks.OBSIDIAN_STAIR_BLOCK);

		LOGGER.info("Finished generating block tags!");
	}
}
