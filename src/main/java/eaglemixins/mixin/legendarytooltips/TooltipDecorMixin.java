package eaglemixins.mixin.legendarytooltips;

import com.anthonyhilyard.legendarytooltips.render.TooltipDecor;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(TooltipDecor.class)
public class TooltipDecorMixin {
    @ModifyConstant(
            method = "drawBorder",
            constant = @Constant(intValue = 16),
            remap = false
    )
    private static int eagleMixins_modifyMaxFrameLvl(int constant){
        return 64;
    }

    @Inject(
            method = "drawBorder",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;color(FFFF)V", shift = At.Shift.AFTER),
            cancellable = true
    )
    private static void eagleMixins_changeTextureSizeTo256x256(int x, int y, int width, int height, ItemStack item, List<String> lines, FontRenderer font, int frameLevel, boolean comparison, int index, CallbackInfo ci){
        int columns = 4;

        float frameU = (float) (frameLevel % columns) * 64F;
        float frameV = (float) (frameLevel / columns) * 16F;

        // Corners
        Gui.drawModalRectWithCustomSizedTexture(x - 6, y - 6, frameU, frameV, 8, 8, 256F, 256F); // top left
        Gui.drawModalRectWithCustomSizedTexture(x + width - 8 + 6, y - 6, frameU + 56, frameV, 8, 8, 256F, 256F); // top right
        Gui.drawModalRectWithCustomSizedTexture(x - 6, y + height - 8 + 6, frameU, frameV + 8, 8, 8, 256F, 256F); // bottom left
        Gui.drawModalRectWithCustomSizedTexture(x + width - 8 + 6, y + height - 8 + 6, frameU + 56, frameV + 8, 8, 8, 256F, 256F); // bottom right

        // Top and bottom middle section
        if (width >= 48) {
            Gui.drawModalRectWithCustomSizedTexture(x + (width / 2) - 24, y - 9, frameU + 8, frameV, 48, 8, 256F, 256F);
            Gui.drawModalRectWithCustomSizedTexture(x + (width / 2) - 24, y + height - 8 + 9, frameU + 8, frameV + 8, 48, 8, 256F, 256F);
        }

        GlStateManager.popMatrix();
        ci.cancel();
    }
}