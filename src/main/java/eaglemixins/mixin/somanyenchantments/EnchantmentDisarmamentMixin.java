package eaglemixins.mixin.somanyenchantments;

import com.Shultrea.Rin.Ench0_4_0.EnchantmentDisarmament;
import com.Shultrea.Rin.Utility_Sector.EnchantmentsUtility;
import com.llamalad7.mixinextras.sugar.Local;
import eaglemixins.util.DisarmingUtility;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EnchantmentDisarmament.class)
public class EnchantmentDisarmamentMixin {
    @Redirect(
            method = "HandleEnchant",
            at = @At(value = "INVOKE", target = "Lcom/Shultrea/Rin/Utility_Sector/EnchantmentsUtility;Disarm(Lnet/minecraft/entity/EntityLivingBase;)V")
    )
    private void stopDisarmingGoodStuff(EntityLivingBase target, @Local EntityLivingBase attacker){
        if(
            !DisarmingUtility.isAllowedToDisarm(target.getHeldItemMainhand(), target, attacker) &&
            !DisarmingUtility.isAllowedToDisarm(target.getHeldItemOffhand(), target, attacker)
        )
            EnchantmentsUtility.Disarm(target);
    }
}
