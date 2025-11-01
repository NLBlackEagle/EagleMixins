package eaglemixins.mixin.fishsundeadrising;

import com.Fishmod.mod_LavaCow.init.ModMobEffects;
import com.Fishmod.mod_LavaCow.util.ModEventHandler;
import net.minecraft.potion.PotionEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ModEventHandler.class)
public class ModEventHandlerMixin {
    @Redirect(
            method = "onEDeath",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/potion/PotionEffect;getAmplifier()I")
    )
    public int eagleMixins_furModEventHandler_getAmplifier(PotionEffect instance){
        if(!instance.getPotion().equals(ModMobEffects.INFESTED)) return instance.getAmplifier();
        //Limit infested effect to lvl 5
        return Math.min(instance.getAmplifier(),4);
    }
}
