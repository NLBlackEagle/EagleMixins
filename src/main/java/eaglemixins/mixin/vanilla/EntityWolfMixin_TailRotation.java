package eaglemixins.mixin.vanilla;

import net.minecraft.entity.passive.EntityWolf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Vanilla lowers a tamed wolf's tail by its missing health, (0.55 - missing * 0.02) * PI, made for 20 max health.
 * A wolf with more (a tamed alpha has double) gets a negative angle when hurt, and its tail swings into its body.
 * Uses the health fraction instead: the same as vanilla at 20 max health, and the same range for any other max health
 */
@Mixin(EntityWolf.class)
public abstract class EntityWolfMixin_TailRotation {
    private static final float VANILLA_MAX_HEALTH = 20.0F;

    @Inject(method = "getTailRotation", at = @At("HEAD"), cancellable = true)
    private void eagleMixins$tailByHealthFraction(CallbackInfoReturnable<Float> cir) {
        EntityWolf wolf = (EntityWolf) (Object) this;
        if (wolf.isAngry() || !wolf.isTamed()) return;
        float max = wolf.getMaxHealth();
        if (max <= 0.0F || max == VANILLA_MAX_HEALTH) return;
        float missing = (1.0F - Math.min(wolf.getHealth(), max) / max) * VANILLA_MAX_HEALTH;
        cir.setReturnValue((0.55F - missing * 0.02F) * (float) Math.PI);
    }
}
