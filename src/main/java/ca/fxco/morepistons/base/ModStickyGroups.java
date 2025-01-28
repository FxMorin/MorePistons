package ca.fxco.morepistons.base;

import ca.fxco.pistonlib.api.pistonLogic.sticky.StickRules;
import ca.fxco.pistonlib.api.pistonLogic.sticky.StickyGroup;
import ca.fxco.pistonlib.api.pistonLogic.sticky.StickyGroups;

import static ca.fxco.morepistons.MorePistons.id;

public class ModStickyGroups {

    public static final StickyGroup TEMPLATE_GROUP = StickyGroups.register(
            id("template_group"),
            new StickyGroup(StickRules.STRICT_SAME)
    );

    public static void bootstrap() {}
}
