package eaglemixins.mixin.cookingforblockheads;

import net.blay09.mods.cookingforblockheads.client.gui.GuiCounter;
import net.blay09.mods.cookingforblockheads.tile.TileCounter;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Container;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiCounter.class)
public abstract class GuiCounterMixin extends GuiContainer {

    @Final
    @Shadow(remap = false)
    private TileCounter tileCounter;

    public GuiCounterMixin(Container inventorySlotsIn) {
        super(inventorySlotsIn);
    }

    @Inject(
            method = "drawGuiContainerForegroundLayer",
            at = @At("HEAD"),
            cancellable = true
    )
    public void drawGuiContainerForegroundLayer(int mouseX, int mouseY, CallbackInfo ci) {
        this.fontRenderer.drawString(this.tileCounter.getDisplayName().getUnformattedText(), 8, 6, 4210752);
        this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
        ci.cancel();
    }
}
