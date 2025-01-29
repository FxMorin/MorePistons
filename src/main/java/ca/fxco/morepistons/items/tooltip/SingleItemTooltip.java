package ca.fxco.morepistons.items.tooltip;

import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.component.BundleContents;

public record SingleItemTooltip(BundleContents contents) implements TooltipComponent {
}
