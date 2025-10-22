package eaglemixins.mixin.nuclearcraft;

import nc.radiation.RadPotionEffects;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(RadPotionEffects.class)
public abstract class MixinRadPotionEffects {

    @ModifyArg(
            method = "init",
            at = @At(value = "INVOKE", target = "Lnc/radiation/RadPotionEffects;parseEffects([Ljava/lang/String;Ljava/util/List;Ljava/util/List;I)V" ),
            remap = false,
            slice = @Slice(from=@At(value = "INVOKE", target = "Lnc/radiation/RadPotionEffects;parseEffects([Ljava/lang/String;Ljava/util/List;Ljava/util/List;I)V", ordinal = 1))
    ) private static int eaglemixins$rawMobBuffDuration(int rawDuration) {

        return 1200;
    }

    @Redirect(
            method = "parseEffects",
            at = @At(
                    value = "INVOKE",
                    target = "Lnc/util/PotionHelper;newEffect(Lnet/minecraft/potion/Potion;II)Lnet/minecraft/potion/PotionEffect;"
            ),
            remap = false
    )
    private static PotionEffect eaglemixins$showParticles(Potion potion, int amplifier, int duration) {
        return new PotionEffect(potion, duration, amplifier, false, true);
    }
}
