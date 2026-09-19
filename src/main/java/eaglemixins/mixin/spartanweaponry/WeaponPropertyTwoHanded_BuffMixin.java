package eaglemixins.mixin.spartanweaponry;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.oblivioussp.spartanweaponry.api.weaponproperty.WeaponProperty;
import com.oblivioussp.spartanweaponry.api.weaponproperty.WeaponPropertyTwoHanded;
import eaglemixins.config.ForgeConfigHandler;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(WeaponPropertyTwoHanded.class)
public abstract class WeaponPropertyTwoHanded_BuffMixin extends WeaponProperty {
    public WeaponPropertyTwoHanded_BuffMixin(String propType, String propModId, int propLevel, float propMagnitude) {
        super(propType, propModId, propLevel, propMagnitude);
    }

    //Inverse of the Two-Handed debuff: an empty off-hand (the debuff not being active) grants a final damage buff instead.
    @ModifyReturnValue(method = "modifyDamageDealt", at = @At("RETURN"), remap = false)
    private float eagleMixins_spartanWeaponryWeaponPropertyTwoHanded_modifyDamageDealt(float original, @Local(name = "attacker") EntityLivingBase attacker) {
        // already debuffed
        if (!attacker.getHeldItemMainhand().isEmpty() && !attacker.getHeldItemOffhand().isEmpty()) return original;

        float buff;
        switch(this.getLevel()){
            case 0: return original;
            case 1: buff = ForgeConfigHandler.weapondamage.spartanTwoHandedBuffLevel1; break;
            default: buff = ForgeConfigHandler.weapondamage.spartanTwoHandedBuffLevel2; break;
        }

        return original * (1.0F + buff);
    }
}