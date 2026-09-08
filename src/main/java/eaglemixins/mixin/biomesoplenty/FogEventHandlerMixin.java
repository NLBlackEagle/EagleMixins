package eaglemixins.mixin.biomesoplenty;

import biomesoplenty.common.handler.FogEventHandler;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.ref.WeakReference;

@Mixin(value = FogEventHandler.class, remap = false)
public abstract class FogEventHandlerMixin {
    @Shadow
    private static boolean fogInit;

    @Shadow
    private static void renderFog(int fogMode, float distance, float start) {
        throw new AssertionError();
    }

    @Unique
    private static WeakReference<World> eaglemixins$fogWorld = new WeakReference<>(null);

    @Unique
    private static long eaglemixins$fogX;

    @Unique
    private static long eaglemixins$fogZ;

    @Unique
    private static int eaglemixins$farPlaneDistance;

    @Unique
    private static float eaglemixins$fogDistance;

    @Unique
    private static float eaglemixins$fogStart;

    @Unique
    private static boolean eaglemixins$fogValid;

    @Inject(method = "onRenderFog", at = @At("HEAD"), cancellable = true)
    private void eaglemixins$reuseFog(EntityViewRenderEvent.RenderFogEvent event, CallbackInfo ci) {
        Entity entity = event.getEntity();
        World world = entity.world;
        long x = Double.doubleToLongBits(entity.posX);
        long z = Double.doubleToLongBits(entity.posZ);
        int farPlaneDistance = Float.floatToIntBits(event.getFarPlaneDistance());

        if (eaglemixins$fogValid
                && eaglemixins$fogWorld.get() == world
                && eaglemixins$fogX == x
                && eaglemixins$fogZ == z
                && eaglemixins$farPlaneDistance == farPlaneDistance) {
            renderFog(event.getFogMode(), eaglemixins$fogDistance, eaglemixins$fogStart);
            ci.cancel();
            return;
        }

        if (eaglemixins$fogWorld.get() != world) {
            eaglemixins$fogWorld = new WeakReference<>(world);
        }

        eaglemixins$fogX = x;
        eaglemixins$fogZ = z;
        eaglemixins$farPlaneDistance = farPlaneDistance;
        eaglemixins$fogValid = false;
        fogInit = false;
    }

    @Redirect(method = "onRenderFog", at = @At(value = "INVOKE", target = "Lbiomesoplenty/common/handler/FogEventHandler;renderFog(IFF)V"))
    private void eaglemixins$cacheFog(int fogMode, float distance, float start) {
        eaglemixins$fogDistance = distance;
        eaglemixins$fogStart = start;
        eaglemixins$fogValid = true;
        renderFog(fogMode, distance, start);
    }

    @Redirect(method = "renderFog", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glFogf(IF)V"))
    private static void eaglemixins$captureFog(int parameter, float value) {
        if (parameter == GL11.GL_FOG_START) {
            GlStateManager.setFogStart(value);
        } else if (parameter == GL11.GL_FOG_END) {
            GlStateManager.setFogEnd(value);
        } else {
            GL11.glFogf(parameter, value);
        }
    }
}
