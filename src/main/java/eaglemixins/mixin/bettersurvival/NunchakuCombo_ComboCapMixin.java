package eaglemixins.mixin.bettersurvival;

import com.mujmajnkraft.bettersurvival.capabilities.nunchakucombo.NunchakuCombo;
import eaglemixins.config.ForgeConfigHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(NunchakuCombo.class)
public abstract class NunchakuCombo_ComboCapMixin {

    @ModifyConstant(
            method = "setComboPower",
            constant = @Constant(floatValue = 1.0F),
            remap = false
    )
    private float eagleMixins_betterSurvivalNunchakuCombo_setComboPowerCap(float constant) {
        return ForgeConfigHandler.weapondamage.nunchakuComboCapAmount;
    }
}
