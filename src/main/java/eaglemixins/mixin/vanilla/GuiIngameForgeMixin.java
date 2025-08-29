package eaglemixins.mixin.vanilla;

import eaglemixins.potion.PotionRadiationSickness;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.GuiIngameForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GuiIngameForge.class)
public abstract class GuiIngameForgeMixin extends GuiIngame {
    @Unique
    private static final ResourceLocation eagleMixins$icons = new ResourceLocation("eaglemixins:textures/gui/icons.png");

    public GuiIngameForgeMixin(Minecraft mcIn) {
        super(mcIn);
    }

    @Redirect(
            method = "renderHealth",
            at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/GuiIngameForge;bind(Lnet/minecraft/util/ResourceLocation;)V"),
            remap = false
    )
    public void eagleMixins_renderHealth_bind(GuiIngameForge instance, ResourceLocation res) {
        if (eagleMixins$shouldRenderCustomHearts()) {
            mc.getTextureManager().bindTexture(eagleMixins$icons);
        } else {
            mc.getTextureManager().bindTexture(ICONS);
        }
    }

    @Redirect(method = "renderHealth",
    at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/GuiIngameForge;drawTexturedModalRect(IIIIII)V", ordinal = 1))
    public void eagleMixins_renderHealth_drawTexturedModalRect_1(GuiIngameForge instance, int x, int y, int textureX, int textureY, int width, int height) {
        eagleMixins$drawTexturedModalRect(x, y, textureX, textureY, width, height);
    }

    @Redirect(method = "renderHealth",
            at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/GuiIngameForge;drawTexturedModalRect(IIIIII)V", ordinal = 2))
    public void eagleMixins_renderHealth_drawTexturedModalRect_2(GuiIngameForge instance, int x, int y, int textureX, int textureY, int width, int height) {
        eagleMixins$drawTexturedModalRect(x, y, textureX, textureY, width, height);
    }

    @Redirect(method = "renderHealth",
            at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/GuiIngameForge;drawTexturedModalRect(IIIIII)V", ordinal = 5))
    public void eagleMixins_renderHealth_drawTexturedModalRect_5(GuiIngameForge instance, int x, int y, int textureX, int textureY, int width, int height) {
        eagleMixins$drawTexturedModalRect(x, y, textureX, textureY, width, height);
    }

    @Redirect(method = "renderHealth",
            at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/GuiIngameForge;drawTexturedModalRect(IIIIII)V", ordinal = 6))
    public void eagleMixins_renderHealth_drawTexturedModalRect_6(GuiIngameForge instance, int x, int y, int textureX, int textureY, int width, int height) {
        eagleMixins$drawTexturedModalRect(x, y, textureX, textureY, width, height);
    }

    @Unique
    private void eagleMixins$drawTexturedModalRect(int x, int y, int textureX, int textureY, int width, int height) {
        if (eagleMixins$shouldRenderCustomHearts()) {
            drawTexturedModalRect(x, y, textureX + 126, textureY, width, height);
        } else {
            drawTexturedModalRect(x, y, textureX, textureY, width, height);
        }
    }

    @Unique
    private boolean eagleMixins$shouldRenderCustomHearts() {
        EntityPlayer player = (EntityPlayer)this.mc.getRenderViewEntity();
        assert player != null;
        if (player.isPotionActive(MobEffects.POISON) || player.isPotionActive(MobEffects.WITHER)) {
            return false;
        }
        PotionEffect radiationSickness = player.getActivePotionEffect(PotionRadiationSickness.INSTANCE);
        if (radiationSickness == null || radiationSickness.getAmplifier() < 2) {
            return false;
        } else if (radiationSickness.getAmplifier() > 2) {
            return true;
        }
        return player.getHealth() > 0.65f * player.getMaxHealth();
    }
}
