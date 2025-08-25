package eaglemixins.mixin.legendarytooltips;

import com.anthonyhilyard.legendarytooltips.render.TooltipDecor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(TooltipDecor.class)
public class TooltipDecorMixin {

    @Inject(method = "drawBorder", at = @At("HEAD"), remap = false, cancellable = true)
    private static void eaglemixins$drawCustomFrame(
            int x, int y, int width, int height,
            ItemStack item, List<String> lines, FontRenderer font,
            int frameLevel, boolean comparison, int index,
            CallbackInfo ci) {

        if (frameLevel < 0 || frameLevel >= 64) return;

        final int frameWidth = 64;
        final int frameHeight = 16;
        final int columns = 4;
        final int textureSize = 256;

        int frameU = (frameLevel % columns) * frameWidth;
        int frameV = (frameLevel / columns) * frameHeight;

        Minecraft mc = Minecraft.getMinecraft();
        mc.getTextureManager().bindTexture(new ResourceLocation("legendarytooltips", "textures/gui/tooltip_borders.png"));

        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0, 0.0, 410.0F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        // Corners
        Gui.drawModalRectWithCustomSizedTexture(x - 6, y - 6, frameU, frameV, 8, 8, textureSize, textureSize); // top left
        Gui.drawModalRectWithCustomSizedTexture(x + width - 8 + 6, y - 6, frameU + 56, frameV, 8, 8, textureSize, textureSize); // top right
        Gui.drawModalRectWithCustomSizedTexture(x - 6, y + height - 8 + 6, frameU, frameV + 8, 8, 8, textureSize, textureSize); // bottom left
        Gui.drawModalRectWithCustomSizedTexture(x + width - 8 + 6, y + height - 8 + 6, frameU + 56, frameV + 8, 8, 8, textureSize, textureSize); // bottom right

        // Top and bottom middle section
        if (width >= 48) {
            Gui.drawModalRectWithCustomSizedTexture(x + (width / 2) - 24, y - 9, frameU + 8, frameV, 48, 8, textureSize, textureSize);
            Gui.drawModalRectWithCustomSizedTexture(x + (width / 2) - 24, y + height - 8 + 9, frameU + 8, frameV + 8, 48, 8, textureSize, textureSize);
        }

        GlStateManager.popMatrix();
        ci.cancel();
    }
}