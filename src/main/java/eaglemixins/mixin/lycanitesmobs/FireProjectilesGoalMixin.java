package eaglemixins.mixin.lycanitesmobs;

import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.BaseProjectileEntity;
import com.lycanitesmobs.core.entity.goals.BaseGoal;
import com.lycanitesmobs.core.entity.goals.actions.abilities.FireProjectilesGoal;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * FireProjectilesGoal (used by Conba, Chupacabra and some bosses) fires at its target without any line of sight check,
 * so e.g. Conbas keep lobbing poop at players behind walls. Non-boss hosts now only fire when they can see the target.
 * Bosses are left alone, LycanitesTweaks handles their targeting.
 */
@Mixin(FireProjectilesGoal.class)
public abstract class FireProjectilesGoalMixin extends BaseGoal {
    public FireProjectilesGoalMixin(BaseCreatureEntity host) {
        super(host);
    }

    @Inject(method = "fireProjectile(Lnet/minecraft/entity/Entity;)Lcom/lycanitesmobs/core/entity/BaseProjectileEntity;", at = @At("HEAD"), cancellable = true, remap = false)
    private void eagleMixins$requireLineOfSight(Entity target, CallbackInfoReturnable<BaseProjectileEntity> cir) {
        if (target == null || this.host == null || this.host.isBoss()) return;
        if (!this.host.canEntityBeSeen(target)) cir.setReturnValue(null);
    }
}
