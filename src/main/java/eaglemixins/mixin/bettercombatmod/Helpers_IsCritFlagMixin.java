package eaglemixins.mixin.bettercombatmod;

import bettercombat.mod.util.Helpers;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import eaglemixins.util.IDamageSource_IsCritFlagMixin;
import net.minecraft.util.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Helpers.class)
public class Helpers_IsCritFlagMixin {

    @ModifyExpressionValue(
            method = "attackTargetEntityItem",
            at = @At(value = "INVOKE", target = "Lbettercombat/mod/event/RLCombatModifyDamageEvent$Post;getDamageSource()Lnet/minecraft/util/DamageSource;"),
            remap = false
    )
    private static DamageSource eagleMixins$rlCombatHelpers_attackTargetEntityItemMeleeIsCrit(DamageSource meleeDamageSource, @Local(name = "isCrit") boolean isCrit){
        if(isCrit && meleeDamageSource instanceof IDamageSource_IsCritFlagMixin) {
            ((IDamageSource_IsCritFlagMixin) meleeDamageSource).eagleMixins$setCrit(isCrit);
        }
        return meleeDamageSource;
    }

    @ModifyExpressionValue(
            method = "attackTargetEntityItem",
            at = @At(value = "INVOKE", target = "Lbettercombat/mod/event/RLCombatSweepEvent;getSweepingDamageSource()Lnet/minecraft/util/DamageSource;"),
            remap = false
    )
    private static DamageSource eagleMixins$rlCombatHelpers_attackTargetEntityItemSweepIsCrit(DamageSource sweepDamageSource, @Local(name = "isCrit") boolean isCrit){
        if(isCrit && sweepDamageSource instanceof IDamageSource_IsCritFlagMixin) {
            ((IDamageSource_IsCritFlagMixin) sweepDamageSource).eagleMixins$setCrit(isCrit);
        }
        return sweepDamageSource;
    }
}