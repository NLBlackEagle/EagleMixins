package eaglemixins.mixin.setbonus;

import com.fantasticsource.mctools.potions.FantasticPotionEffect;
import com.fantasticsource.setbonus.common.bonuselements.BonusElementPotionEffect;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.ArrayList;

// Removes SetBonus PotionEffects no matter what, instead of only if duration and amplifier are small enough to count to the bonus
// This restores behavior to how it was in old setbonus
@Mixin(BonusElementPotionEffect.class)
public abstract class BonusElementPotionEffectMixin {
    @Shadow(remap = false) public ArrayList<FantasticPotionEffect> potions;

    @WrapMethod(method = "deactivate", remap = false)
    private void eaglemixins_removeAllBonusEffects(EntityPlayer player, Operation<Void> original){
        for(FantasticPotionEffect potion : this.potions)
            if(player.isPotionActive(potion.getPotion()))
                player.removePotionEffect(potion.getPotion());
        //don't run original code
    }
}
