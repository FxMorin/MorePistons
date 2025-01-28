package ca.fxco.pistonmodtemplate.base;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

import java.util.Map;
import java.util.function.Function;

import static ca.fxco.pistonmodtemplate.PistonModTemplate.id;

public class ModItems {

    public static final Item TEMPLATE_BLOCK = register(
            id("template_block"),
            properties -> new BlockItem(ModBlocks.TEMPLATE_BLOCK, properties)
    );

    private static <T extends Item> T register(ResourceLocation id, Function<Item.Properties, T> item) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id);
        return Registry.register(BuiltInRegistries.ITEM, key, item.apply((new Item.Properties()).setId(key)));
    }

    public static void bootstrap() {}
}
