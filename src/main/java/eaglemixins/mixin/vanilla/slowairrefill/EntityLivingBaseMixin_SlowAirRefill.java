package eaglemixins.mixin.vanilla.slowairrefill;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Makes player air refill gradually after surfacing (1.13+ style)
 * instead of instantly resetting to full in 1.12.x.
 */
@Mixin(EntityLivingBase.class)
public abstract class EntityLivingBaseMixin_SlowAirRefill {

    /**
     * Redirect the call to setAir(I) inside EntityLivingBase#onEntityUpdate.
     * Vanilla calls setAir(300) when not drowning; we intercept that and
     * do a gradual refill for players. All other setAir calls pass through.
     */
    @WrapOperation(
            method = "onEntityUpdate",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityLivingBase;setAir(I)V", ordinal = 0)
    )
    private void eaglemixins_gradualAirRefill(EntityLivingBase self, int originalAir, Operation<Void> original) {
        // Only change behavior for players, and only for the instant-refill case (air >= 300)
        if (self instanceof EntityPlayer && originalAir >= 300) {
            final int maxAir = 300;          // vanilla max in 1.12
            final int regenPerTick = 3;      // ~1.13+ feel; make configurable if you like

            int current = self.getAir();
            int next = Math.min(current + regenPerTick, maxAir);

            // Apply our gradual refill
            original.call(self, next);
        } else {
            original.call(self, originalAir);
        }
    }
}
