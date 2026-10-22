package eaglemixins.client.render;

import eaglemixins.EagleMixins;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.lang.reflect.Method;

/**
 * Draws Trinkets and Baubles (xat) dragon wings and horns on any biped entity, through reflection because the
 * renderers only exist in xat 0.33+ and this mod compiles against an older version. Missing = nothing is drawn.
 */
@SideOnly(Side.CLIENT)
public final class XatRaceRenderCompat {

    private static boolean initialized = false;
    @Nullable private static Part wings, horns, hornsInverted, dragonHorns;

    private static final class Part {
        final Object instance;
        final Method render;

        Part(Object instance, Method render) {
            this.instance = instance;
            this.render = render;
        }
    }

    private static void init() {
        initialized = true;
        if (!Loader.isModLoaded("xat")) return;
        wings = find("xzeroair.trinkets.client.races.dragon.RaceDragonWings", int.class, int.class, int.class, int.class);
        horns = find("xzeroair.trinkets.client.races.generic.RaceHorns", int.class, int.class);
        hornsInverted = find("xzeroair.trinkets.client.races.generic.RaceHornsInverted", int.class, int.class);
        dragonHorns = find("xzeroair.trinkets.client.races.dragon.RaceDragonHorns", int.class, int.class);
        if (wings == null) EagleMixins.LOGGER.info("Trinkets and Baubles dragon wing renderer not found (needs 0.33+), player bosses render without wings");
    }

    /** render(entity, renderer, isFake, isSlim, partialTicks, scale, extra int args...) */
    @Nullable
    private static Part find(String className, Class<?>... extraArgs) {
        try {
            Class<?> clazz = Class.forName(className);
            Class<?>[] args = new Class<?>[6 + extraArgs.length];
            args[0] = EntityLivingBase.class;
            args[1] = RenderLivingBase.class;
            args[2] = boolean.class;
            args[3] = boolean.class;
            args[4] = float.class;
            args[5] = float.class;
            System.arraycopy(extraArgs, 0, args, 6, extraArgs.length);
            return new Part(clazz.getField("INSTANCE").get(null), clazz.getMethod("render", args));
        } catch (ReflectiveOperationException | LinkageError e) {
            return null;
        }
    }

    public static void renderWings(EntityLivingBase entity, RenderLivingBase<?> renderer, boolean slim, float partialTicks, float scale, int primary, int secondary) {
        if (!initialized) init();
        invoke(wings, entity, renderer, slim, partialTicks, scale, 0, primary, secondary, 0);
    }

    /** style: INVERTED, HORNS or DRAGON */
    public static void renderHorns(EntityLivingBase entity, RenderLivingBase<?> renderer, boolean slim, float partialTicks, float scale, String style, int color) {
        if (!initialized) init();
        switch (style) {
            case "INVERTED": invoke(hornsInverted, entity, renderer, slim, partialTicks, scale, 0, color); break;
            case "HORNS": invoke(horns, entity, renderer, slim, partialTicks, scale, 1, color); break;
            case "DRAGON": invoke(dragonHorns, entity, renderer, slim, partialTicks, scale, 2, color); break;
            default: break;
        }
    }

    private static void invoke(@Nullable Part part, EntityLivingBase entity, RenderLivingBase<?> renderer, boolean slim, float partialTicks, float scale, Object... extra) {
        if (part == null) return;
        Object[] args = new Object[6 + extra.length];
        args[0] = entity;
        args[1] = renderer;
        args[2] = false;
        args[3] = slim;
        args[4] = partialTicks;
        args[5] = scale;
        System.arraycopy(extra, 0, args, 6, extra.length);
        try {
            part.render.invoke(part.instance, args);
        } catch (ReflectiveOperationException e) {
            EagleMixins.LOGGER.error("Trinkets and Baubles race renderer failed, disabling it", e);
            if (part == wings) wings = null;
            else if (part == horns) horns = null;
            else if (part == hornsInverted) hornsInverted = null;
            else if (part == dragonHorns) dragonHorns = null;
        }
    }

    private XatRaceRenderCompat() {}
}
