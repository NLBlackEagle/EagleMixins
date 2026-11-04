package eaglemixins.client.render;

import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import xzeroair.trinkets.client.races.dragon.RaceDragonHorns;
import xzeroair.trinkets.client.races.dragon.RaceDragonWings;
import xzeroair.trinkets.client.races.generic.RaceHorns;
import xzeroair.trinkets.client.races.generic.RaceHornsInverted;

/**
 * Draws Trinkets and Baubles (xat, 0.33+) dragon wings and horns on any biped entity.
 * Without Trinkets and Baubles nothing is drawn; its classes are only touched inside Xat.
 */
@SideOnly(Side.CLIENT)
public final class XatRaceRenderCompat {

    private static final boolean XAT_LOADED = Loader.isModLoaded("xat");

    public static void renderWings(EntityLivingBase entity, RenderLivingBase<?> renderer, boolean slim, float partialTicks, float scale, int primary, int secondary) {
        if (XAT_LOADED) Xat.renderWings(entity, renderer, slim, partialTicks, scale, primary, secondary);
    }

    /** style: INVERTED, HORNS or DRAGON */
    public static void renderHorns(EntityLivingBase entity, RenderLivingBase<?> renderer, boolean slim, float partialTicks, float scale, String style, int color) {
        if (XAT_LOADED) Xat.renderHorns(entity, renderer, slim, partialTicks, scale, style, color);
    }

    /** Kept separate so Trinkets and Baubles classes are only loaded when the mod is present. */
    private static final class Xat {
        static void renderWings(EntityLivingBase entity, RenderLivingBase<?> renderer, boolean slim, float partialTicks, float scale, int primary, int secondary) {
            RaceDragonWings.INSTANCE.render(entity, renderer, false, slim, partialTicks, scale, 0, primary, secondary, 0);
        }

        static void renderHorns(EntityLivingBase entity, RenderLivingBase<?> renderer, boolean slim, float partialTicks, float scale, String style, int color) {
            switch (style) {
                case "INVERTED": RaceHornsInverted.INSTANCE.render(entity, renderer, false, slim, partialTicks, scale, 0, color); break;
                case "HORNS": RaceHorns.INSTANCE.render(entity, renderer, false, slim, partialTicks, scale, 1, color); break;
                case "DRAGON": RaceDragonHorns.INSTANCE.render(entity, renderer, false, slim, partialTicks, scale, 2, color); break;
                default: break;
            }
        }
    }

    private XatRaceRenderCompat() {}
}
