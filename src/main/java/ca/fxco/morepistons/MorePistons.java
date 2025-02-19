package ca.fxco.morepistons;

import ca.fxco.morepistons.base.*;
import ca.fxco.pistonlib.api.PistonLibInitializer;
import ca.fxco.pistonlib.api.config.ConfigFieldEntrypoint;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

public class MorePistons implements ModInitializer, PistonLibInitializer, ConfigFieldEntrypoint {

    public static final String MOD_ID = "morepistons";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {}

    @Override
    public void initialize() {}

    @Override
    public Map<String, List<Field>> getConfigFields() {
        return Map.of("pistonlib", List.of(MorePistonsConfig.class.getFields()));
    }

    @Override
    public void registerPistonFamilies() {
        ModPistonFamilies.bootstrap();
    }

    @Override
    public void registerStickyGroups() {
        ModStickyGroups.bootstrap();
    }

    @Override
    public void bootstrap() {
        ModBlocks.bootstrap();
        ModBlockEntities.bootstrap();
        ModDataComponents.bootstrap();
        ModItems.bootstrap();
        ModMenus.bootstrap();
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            ModCreativeModeTabs.bootstrap();
            ModScreens.bootstrap();
        }
    }

    public static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, name);
    }
}
