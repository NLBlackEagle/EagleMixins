package eaglemixins.mixin.vanilla;

import eaglemixins.client.AlphaWolfClientHandler;
import net.minecraft.client.renderer.entity.RenderWolf;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Alpha wolves (Wolf Packs) use their own skin */
@Mixin(RenderWolf.class)
public abstract class RenderWolfMixin_AlphaSkin {

    @Inject(method = "getEntityTexture(Lnet/minecraft/entity/passive/EntityWolf;)Lnet/minecraft/util/ResourceLocation;", at = @At("HEAD"), cancellable = true)
    private void eagleMixins$alphaSkin(EntityWolf entity, CallbackInfoReturnable<ResourceLocation> cir) {
        ResourceLocation texture = AlphaWolfClientHandler.getAlphaTexture(entity);
        if (texture != null) cir.setReturnValue(texture);
    }
}
