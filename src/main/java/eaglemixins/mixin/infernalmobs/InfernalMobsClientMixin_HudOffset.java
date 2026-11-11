package eaglemixins.mixin.infernalmobs;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import eaglemixins.client.gui.InfernalHudOffset;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Moves the Infernal Mobs health HUD down below the boss bars or the Champions health HUD instead of drawing over them.
 * See InfernalHudOffset.
 */
@Pseudo
@Mixin(targets = "atomicstryker.infernalmobs.client.InfernalMobsClient", remap = false)
public abstract class InfernalMobsClientMixin_HudOffset {

    @Unique
    private boolean eaglemixins$shifted = false;

    @ModifyExpressionValue(
            method = "onPreRenderGameOverlay",
            at = @At(value = "INVOKE", target = "Latomicstryker/infernalmobs/client/InfernalMobsClient;getEntityCrosshairOver(FLnet/minecraft/client/Minecraft;)Lnet/minecraft/entity/Entity;")
    )
    private Entity eaglemixins$shiftHud(Entity target) {
        int offset = InfernalHudOffset.getOffset(target);
        if (offset > 0) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(0.0F, offset, 0.0F);
            eaglemixins$shifted = true;
        }
        return target;
    }

    @Inject(method = "onPreRenderGameOverlay", at = @At("RETURN"))
    private void eaglemixins$restoreHud(CallbackInfo ci) {
        if (eaglemixins$shifted) {
            GlStateManager.popMatrix();
            eaglemixins$shifted = false;
        }
    }
}
