package eaglemixins.mixin.biomesoplenty;

import biomesoplenty.common.handler.FogEventHandler;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.lang.ref.WeakReference;

@Mixin(value = FogEventHandler.class, remap = false)
public abstract class FogEventHandlerMixin {
    @Shadow private static boolean fogInit;
    @Shadow private static void renderFog(int fogMode, float distance, float start) {
        throw new AssertionError("Failed to shadow BoP FogEventHandler.renderFog");
    }

    @Unique private static WeakReference<World> eaglemixins$fogWorld = new WeakReference<>(null);
    @Unique private static long eaglemixins$fogX;
    @Unique private static long eaglemixins$fogZ;
    @Unique private static int eaglemixins$farPlaneDistance;
    @Unique private static float eaglemixins$fogDistance;
    @Unique private static float eaglemixins$fogStart;
    @Unique private static boolean eaglemixins$fogValid;

    @WrapMethod(method = "onRenderFog")
    private void eaglemixins$reuseFog(EntityViewRenderEvent.RenderFogEvent event, Operation<Void> original) {
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

        original.call(event);
    }

    @WrapOperation(method = "onRenderFog", at = @At(value = "INVOKE", target = "Lbiomesoplenty/common/handler/FogEventHandler;renderFog(IFF)V"))
    private void eaglemixins$cacheFog(int fogMode, float distance, float start, Operation<Void> original) {
        eaglemixins$fogDistance = distance;
        eaglemixins$fogStart = start;
        eaglemixins$fogValid = true;
        original.call(fogMode, distance, start);
    }

    @WrapOperation(method = "renderFog", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glFogf(IF)V"))
    private static void eaglemixins$captureFog(int parameter, float value, Operation<Void> original) {
        if (parameter == GL11.GL_FOG_START) {
            GlStateManager.setFogStart(value);
        } else if (parameter == GL11.GL_FOG_END) {
            GlStateManager.setFogEnd(value);
        } else {
            original.call(parameter, value);
        }
    }
}
