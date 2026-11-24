package eaglemixins.mixin.rustic;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import rustic.client.renderer.LayerIronSkin;

/** Granite Guard (Rustic Iron Skin) overlay: granite texture instead of the iron block, and more see-through */
@Mixin(LayerIronSkin.class)
public abstract class LayerIronSkinMixin {

    @Unique private static final ResourceLocation eaglemixins$GRANITE = new ResourceLocation("textures/blocks/stone_granite.png");

    /** Rustic uses 0.75-0.9 alpha depending on amplifier; this scales it down to roughly 0.3-0.36 */
    @Unique private static final float eaglemixins$ALPHA_SCALE = 0.4F;

    @ModifyArg(
            method = "doRenderLayer",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/RenderLivingBase;bindTexture(Lnet/minecraft/util/ResourceLocation;)V")
    )
    private ResourceLocation eaglemixins_graniteTexture(ResourceLocation original) {
        return eaglemixins$GRANITE;
    }

    // First color call sets the overlay tint, the second one resets to white afterwards
    @Redirect(
            method = "doRenderLayer",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;color(FFFF)V", ordinal = 0)
    )
    private void eaglemixins_graniteTint(float red, float green, float blue, float alpha) {
        // Full white keeps the granite texture's own color, Rustic's 0.9 grey would dull it
        GlStateManager.color(1.0F, 1.0F, 1.0F, alpha * eaglemixins$ALPHA_SCALE);
    }
}
