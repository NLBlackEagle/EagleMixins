package eaglemixins.mixin.vanilla;

import eaglemixins.handlers.WolfPackHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityWolf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Alpha wolves (Wolf Packs) are called "Alpha Wolf", e.g. in death messages, without a visible name tag */
@Mixin(Entity.class)
public abstract class EntityMixin_AlphaWolfName {

    @Inject(method = "getName", at = @At("HEAD"), cancellable = true)
    private void eagleMixins$alphaWolfName(CallbackInfoReturnable<String> cir) {
        Entity self = (Entity) (Object) this;
        if (!(self instanceof EntityWolf)) return;
        String name = WolfPackHandler.getAlphaName((EntityWolf) self);
        if (name != null) cir.setReturnValue(name);
    }
}
