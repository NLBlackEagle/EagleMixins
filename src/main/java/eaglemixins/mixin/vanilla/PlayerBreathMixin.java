package eaglemixins.mixin.vanilla;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Makes player air refill gradually after surfacing (1.13+ style)
 * instead of instantly resetting to full in 1.12.x.
 */
@Mixin(EntityLivingBase.class)
public abstract class PlayerBreathMixin {

    /**
     * Redirect the call to setAir(I) inside EntityLivingBase#onEntityUpdate.
     * Vanilla calls setAir(300) when not drowning; we intercept that and
     * do a gradual refill for players. All other setAir calls pass through.
     */
    @Redirect(
            method = "onEntityUpdate",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/EntityLivingBase;setAir(I)V"
            )
    )
    private void eaglemixins$gradualAirRefill(EntityLivingBase self, int air) {
        // Only change behavior for players, and only for the instant-refill case (air >= 300)
        if (self instanceof EntityPlayer && air >= 300) {
            final int maxAir = 300;          // vanilla max in 1.12
            final int regenPerTick = 3;      // ~1.13+ feel; make configurable if you like

            int current = self.getAir();
            int next = Math.min(current + regenPerTick, maxAir);

            // Apply our gradual refill
            self.setAir(next);
        } else {
            // Passthrough: drowning decrement, modded adjustments, etc.
            self.setAir(air);
        }
    }
}
