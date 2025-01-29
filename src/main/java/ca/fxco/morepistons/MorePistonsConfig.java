package ca.fxco.morepistons;

import ca.fxco.pistonlib.api.config.Category;
import ca.fxco.pistonlib.api.config.ConfigValue;

public class MorePistonsConfig {

    // ==============
    //    Features
    // ==============

    @ConfigValue(
            desc = "Adds a feature to create packed ice by mering multiple ice blocks together",
            keyword = {"ice", "merging", "packed"},
            category = {Category.FEATURE, Category.MERGING}
    )
    public static boolean doIceMerging = true;

    @ConfigValue(
            desc = "Adds a feature which allows you to merge signs together",
            keyword = {"sign", "merging"},
            category = {Category.FEATURE, Category.MERGING}
    )
    public static boolean doSignMerging = true;

    @ConfigValue(
            desc = "Adds a feature which allows you to merge slabs together",
            keyword = {"slab", "merging"},
            category = {Category.FEATURE, Category.MERGING}
    )
    public static boolean doSlabMerging = true;

    // TODO: Hide item from the creative inventory when disabled, since all crafting blocks placed when disabled won't have a block entity
    @ConfigValue(
            desc = "Toggle the auto crafting block feature. The block will still exist it just wont work if disabled",
            keyword = {"auto", "crafting"},
            category = Category.FEATURE
    )
    public static boolean autoCraftingBlock = true;

    @ConfigValue(
            desc = "Allows the auto crafting table to be movable",
            keyword = {"auto", "crafting", "moving"},
            category = Category.FEATURE
    )
    public static boolean movableAutoCraftingBlock = true;

    @ConfigValue(
            desc = "Allow pistons to pull blocks out of slots other than the output slot",
            keyword = {"auto", "crafting", "extract"},
            category = Category.FEATURE
    )
    public static boolean extractBlocksFromAutoCrafting = true;

    @ConfigValue(
            desc = "Continues ticking furnace type blocks as they are moving",
            keyword = {"ticking", "furnace", "cooking"},
            category = Category.FEATURE
    )
    public static boolean cookWhileMoving = true;

    @ConfigValue(
            desc = "Make the sticky chains strongly sticky, allows them to be pulled along by non-sticky blocks",
            keyword = {"chain", "strong", "sticky"},
            category = Category.FEATURE
    )
    public static boolean strongStickyChains = true;

}
