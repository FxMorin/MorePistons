package ca.fxco.morepistons.mixin;

import ca.fxco.morepistons.items.tooltip.SingleClientBundleTooltip;
import ca.fxco.morepistons.items.tooltip.SingleItemTooltip;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.component.BundleContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientTooltipComponent.class)
public interface ClientTooltipComponentMixin {

    @Inject(
            method = "create(Lnet/minecraft/world/inventory/tooltip/TooltipComponent;)" +
                    "Lnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipComponent;",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void pl$onCreate(TooltipComponent tooltipComponent,
                                    CallbackInfoReturnable<ClientTooltipComponent> cir) {
        if (tooltipComponent instanceof SingleItemTooltip(BundleContents contents)) {
            cir.setReturnValue(new SingleClientBundleTooltip(contents));
        }
    }
}