package eaglemixins.mixin.dynamicsurroundings;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.common.ForgeModContainer;
import org.orecruncher.dsurround.client.handlers.fog.BiomeFogColorCalculator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BiomeFogColorCalculator.class)
public abstract class BiomeFogColorCalculatorMixin {
    @Shadow protected boolean doScan;

    @Unique private int eaglemixins$blendRadius = Integer.MIN_VALUE;

    @Inject(method = "calculate", at = @At("HEAD"), remap = false)
    private void eaglemixins$refreshForBlendRadius(EntityViewRenderEvent.FogColors event, CallbackInfoReturnable<?> cir) {
        GameSettings settings = Minecraft.getMinecraft().gameSettings;
        int[] blendRanges = ForgeModContainer.blendRanges;
        int blendRadius = 6;

        if (settings.fancyGraphics && blendRanges.length > 0) {
            int index = Math.max(0, Math.min(settings.renderDistanceChunks, blendRanges.length - 1));
            blendRadius = blendRanges[index];
        }

        if (this.eaglemixins$blendRadius != blendRadius) {
            this.eaglemixins$blendRadius = blendRadius;
            this.doScan = true;
        }
    }
}
