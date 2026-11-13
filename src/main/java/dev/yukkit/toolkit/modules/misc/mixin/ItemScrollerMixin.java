package dev.yukkit.toolkit.modules.misc.mixin;

import dev.yukkit.toolkit.modules.misc.ItemScroller;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Gives {@link ItemScroller} access to the hovered slot of every container screen. */
@Mixin(AbstractContainerScreen.class)
public abstract class ItemScrollerMixin {
    @Shadow
    protected abstract Slot getHoveredSlot(double mouseX, double mouseY);

    @Shadow
    protected abstract void slotClicked(Slot slot, int slotId, int button, ContainerInput type);

    @Inject(method = "extractContents", at = @At("HEAD"), require = 0)
    private void yukkit$itemScroller(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        try {
            Slot slot = this.getHoveredSlot(mouseX, mouseY);
            if (slot == null || !slot.hasItem()) return;
            if (!ItemScroller.shouldAct(slot.index)) return;
            this.slotClicked(slot, slot.index, 0, ContainerInput.QUICK_MOVE);
        } catch (Throwable ignored) {
            // never break the screen
        }
    }
}
