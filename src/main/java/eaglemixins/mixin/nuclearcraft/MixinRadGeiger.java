package eaglemixins.mixin.nuclearcraft;

import nc.radiation.RadiationRenders;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RadiationRenders.class)
public abstract class MixinRadGeiger {

    @Redirect(method = "addRadiationInfo", at = @At(value = "INVOKE", target = "Lnc/radiation/RadiationHelper;radsPrefix(DZ)Ljava/lang/String;"), remap = false)
    private String eaglemixins$radsPrefix(double rads, boolean rate) {
        final int n = Math.max(1, nc.config.NCConfig.radiation_unit_prefixes);
        final String num = eagleMixins$formatRads(rads, n);
        return num + " " + (rate ? "Rads/t" : "Rads");
    }

    @Unique
    private static String eagleMixins$formatRads(double rads, int precision) {
        int orderOfMagnitude = (int) Math.floor(Math.log10(Math.abs(rads))) + 1;
        int digitsToUse = MathHelper.clamp(precision - orderOfMagnitude, 0 , precision);
        //use format xx.xx for numbers above 1 and 0.xxxx for numbers below 1 (if n=4)
        return String.format(java.util.Locale.ROOT, "%." + digitsToUse + "f", rads);
    }
}
