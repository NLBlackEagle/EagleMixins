package eaglemixins.mixin.vanilla.potionlevelcap;

import eaglemixins.util.PotionLevelCaps;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.PotionEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(EntityLivingBase.class)
public abstract class EntityLivingBaseMixin_PotionLevelCap {

    //Clamp the incoming effect before it is added or combined with an already active one
    @ModifyVariable(
            method = "addPotionEffect",
            at = @At("HEAD"),
            argsOnly = true
    )
    private PotionEffect eaglemixins_capPotionLevel(PotionEffect effect) {
        return PotionLevelCaps.cap(effect);
    }
}
