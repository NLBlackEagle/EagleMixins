package eaglemixins.mixin.srparasites;

import com.dhanantry.scapeandrunparasites.entity.ai.EntityAINearestAttackableTargetStatus;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Parasites look for players with this AI. For invisible players it multiplies the detection range by
 * "Invisible Penalty" * armor visibility (worn armor slots / 4, min 0.1), so without armor the range drops to 10%,
 * even with the penalty set to 1.0. This makes the invisibility check always fail, so parasites ignore invisibility.
 */
@Mixin(EntityAINearestAttackableTargetStatus.class)
public abstract class EntityAINearestAttackableTargetStatus_IgnoreInvisMixin {
    @ModifyExpressionValue(
            method = "getNearest",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayer;isInvisible()Z", remap = true),
            remap = false
    )
    private boolean eagleMixins_srpNearestTargetStatus_ignoreInvisibility(boolean isInvisible) {
        return false;
    }
}
