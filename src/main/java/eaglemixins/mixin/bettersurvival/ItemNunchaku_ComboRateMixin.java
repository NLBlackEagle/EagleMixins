package eaglemixins.mixin.bettersurvival;

import com.llamalad7.mixinextras.sugar.Local;
import com.mujmajnkraft.bettersurvival.capabilities.nunchakucombo.INunchakuCombo;
import com.mujmajnkraft.bettersurvival.items.ItemNunchaku;
import eaglemixins.config.ForgeConfigHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ItemNunchaku.class)
public abstract class ItemNunchaku_ComboRateMixin {

    @ModifyConstant(
            method = "hitEntity",
            constant = @Constant(floatValue = 0.1F, ordinal = 1)
    )
    private float eagleMixins_betterSurvivalItemNunchaku_hitEntityBaseComboRate(float baseComboRate){
        return ForgeConfigHandler.weapondamage.nunchakuComboBaseRate; // Minimum gained per hit
    }

    @ModifyConstant(
            method = "hitEntity",
            constant = @Constant(floatValue = 20F)
    )
    private float eagleMixins_betterSurvivalItemNunchaku_hitEntityEnchantmentComboRate(float baseComboRate){
        // Safe case of divide by zero
        return 1F / ForgeConfigHandler.weapondamage.nunchakuComboEnchantmentRate; // Amount gained from combo enchantment per hit
    }

    @ModifyArg(
            method = "hitEntity",
            at = @At(value = "INVOKE", target = "Lcom/mujmajnkraft/bettersurvival/capabilities/nunchakucombo/INunchakuCombo;setComboPower(F)V", remap = false)
    )
    private float eagleMixins_betterSurvivalItemNunchaku_hitEntityComboStartAmount(float power, @Local INunchakuCombo combo){
        if(combo.getComboTime() == 0) {
            return ForgeConfigHandler.weapondamage.nunchakuComboStartAmount; // Starting amount upon 1st hit
        }

        return power;
    }
}
