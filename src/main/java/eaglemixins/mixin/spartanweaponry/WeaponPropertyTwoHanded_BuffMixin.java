package eaglemixins.mixin.spartanweaponry;

import com.oblivioussp.spartanweaponry.api.ToolMaterialEx;
import com.oblivioussp.spartanweaponry.api.weaponproperty.WeaponProperty;
import com.oblivioussp.spartanweaponry.api.weaponproperty.WeaponPropertyTwoHanded;
import eaglemixins.config.ForgeConfigHandler;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WeaponPropertyTwoHanded.class)
public abstract class WeaponPropertyTwoHanded_BuffMixin {

    /**
     * Inverse of the Two-Handed debuff: an empty off-hand (the debuff not being active) grants a final damage buff instead.
     */
    @Inject(
            method = "modifyDamageDealt",
            at = @At("RETURN"),
            cancellable = true,
            remap = false
    )
    private void eagleMixins_spartanWeaponryWeaponPropertyTwoHanded_modifyDamageDealt(ToolMaterialEx material, float baseDamage, DamageSource source, EntityLivingBase attacker, EntityLivingBase victim, CallbackInfoReturnable<Float> cir) {
        ItemStack mainHand = attacker.getHeldItemMainhand();
        ItemStack offHand = attacker.getHeldItemOffhand();
        if (!mainHand.isEmpty() && !offHand.isEmpty()) return; // debuff branch already handled it, leave as-is

        int level = ((WeaponProperty) (Object) this).getLevel();
        float buff = level >= 2 ? ForgeConfigHandler.weapondamage.spartanTwoHandedBuffLevel2 : ForgeConfigHandler.weapondamage.spartanTwoHandedBuffLevel1;
        if (buff <= 0.0F) return;

        cir.setReturnValue(cir.getReturnValueF() * (1.0F + buff));
    }
}