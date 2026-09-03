package eaglemixins.mixin.vanilla;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import eaglemixins.util.IDamageSource_IsCritFlagMixin;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EntityPlayer.class)
public class EntityPlayer_IsCritFlagMixin {

    @ModifyExpressionValue(
            method = "attackTargetEntityWithCurrentItem",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/DamageSource;causePlayerDamage(Lnet/minecraft/entity/player/EntityPlayer;)Lnet/minecraft/util/DamageSource;", ordinal = 0)
    )
    private DamageSource eagleMixins$vanillaEntityPlayer_attackTargetEntityWithCurrentItemMeleeIsCrit(DamageSource meleeDamageSource, @Local(name = "flag2") boolean isCrit){
        if(isCrit && meleeDamageSource instanceof IDamageSource_IsCritFlagMixin) {
            ((IDamageSource_IsCritFlagMixin) meleeDamageSource).eagleMixins$setCrit(isCrit);
        }
        return meleeDamageSource;
    }

    @ModifyExpressionValue(
            method = "attackTargetEntityWithCurrentItem",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/DamageSource;causePlayerDamage(Lnet/minecraft/entity/player/EntityPlayer;)Lnet/minecraft/util/DamageSource;", ordinal = 1)
    )
    private DamageSource eagleMixins$vanillaEntityPlayer_attackTargetEntityWithCurrentItemSweepIsCrit(DamageSource sweepDamageSource, @Local(name = "flag2") boolean isCrit){
        if(isCrit && sweepDamageSource instanceof IDamageSource_IsCritFlagMixin) {
            ((IDamageSource_IsCritFlagMixin) sweepDamageSource).eagleMixins$setCrit(isCrit);
        }
        return sweepDamageSource;
    }
}