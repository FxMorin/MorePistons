package ca.fxco.pistonmodtemplate.base;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Function;

import static ca.fxco.pistonmodtemplate.PistonModTemplate.id;

public class ModBlocks {

    public static final Block TEMPLATE_BLOCK = register(
            "template_block",
            Block::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BLOCK)
    );

    private static <T extends Block> T register(String name, Function<BlockBehaviour.Properties, T> function,
                                                BlockBehaviour.Properties properties) {
        ResourceKey<Block> resourceKey = ResourceKey.create(Registries.BLOCK, id(name));
        return Registry.register(BuiltInRegistries.BLOCK, resourceKey, function.apply(properties.setId(resourceKey)));
    }

    public static void bootstrap() {}
}
