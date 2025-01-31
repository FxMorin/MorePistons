package ca.fxco.morepistons.datagen;

import ca.fxco.morepistons.MorePistons;
import org.slf4j.Logger;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class MorePistonsDatagen implements DataGeneratorEntrypoint {

    public static final Logger LOGGER = MorePistons.LOGGER;

	@Override
	public void onInitializeDataGenerator(FabricDataGenerator dataGenerator) {
		LOGGER.info("Starting MorePistons datagen...");

		FabricDataGenerator.Pack pack = dataGenerator.createPack();

		pack.addProvider(ModModelProvider::new);
		pack.addProvider(ModRecipeProvider::new);
		pack.addProvider(ModBlockLootTableProvider::new);
		pack.addProvider(ModBlockTagProvider::new);
	}
}
