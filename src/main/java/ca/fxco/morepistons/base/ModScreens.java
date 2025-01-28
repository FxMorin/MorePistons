package ca.fxco.morepistons.base;

import ca.fxco.morepistons.blocks.autoCraftingBlock.AutoCraftingScreen;
import net.minecraft.client.gui.screens.MenuScreens;

public class ModScreens {

    static {
        MenuScreens.register(ModMenus.AUTO_CRAFTING, AutoCraftingScreen::new);
    }

    public static void bootstrap() { }
}
