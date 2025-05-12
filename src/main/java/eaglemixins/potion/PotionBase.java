package eaglemixins.potion;

import eaglemixins.EagleMixins;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class PotionBase extends Potion {

    private final ResourceLocation texture;

    public PotionBase(String name, boolean isBadEffect, int liquidColor) {
        super(isBadEffect,liquidColor);
        this.texture = new ResourceLocation(EagleMixins.MODID, "textures/effects/" + name + ".png");
        this.setRegistryName(EagleMixins.MODID, name);
        this.setPotionName(EagleMixins.MODID + ".effects." + name);
    }

    public ResourceLocation getTexture() { return this.texture; }

    @Override
    public boolean hasStatusIcon() { return true; }

    @SideOnly(Side.CLIENT)
    @Override
    public void renderInventoryEffect(PotionEffect effect, Gui gui, int x, int y, float z) {
        renderInventoryEffect(x, y, effect, Minecraft.getMinecraft());
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void renderInventoryEffect(int x, int y, PotionEffect effect, Minecraft mc) {
        if(getTexture()!=null) {
            Minecraft.getMinecraft().getTextureManager().bindTexture(getTexture());
            Gui.drawModalRectWithCustomSizedTexture(x+6, y+7, 0, 0, 18, 18, 18, 18);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void renderHUDEffect(PotionEffect effect, Gui gui, int x, int y, float z, float alpha) {
        renderHUDEffect(x, y, effect, Minecraft.getMinecraft(), alpha);
    }

    @Override
    public void renderHUDEffect(int x, int y, PotionEffect effect, Minecraft mc, float alpha) {
        if(getTexture()!=null) {
            mc.getTextureManager().bindTexture(getTexture());
            Gui.drawModalRectWithCustomSizedTexture(x+3, y+3, 0, 0, 18, 18, 18, 18);
        }
    }

    @Override
    public boolean shouldRenderHUD(PotionEffect effect) {
        if (effect.getEffectName() == "rad_weakness") { return false; } else {
            return true;
        }
    }

    @Override
    public boolean shouldRender(PotionEffect effect) {
        if (effect.getEffectName() == "rad_weakness") { return false; } else {
            return true;
        }
    }

    @Override
    public boolean shouldRenderInvText(PotionEffect effect) {
        if (effect.getEffectName() == "rad_weakness") { return false; } else {
            return true;
        }
    }
}