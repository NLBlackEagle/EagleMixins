package eaglemixins.mixin.nuclearcraft;

import nc.radiation.RadiationRenders;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RadiationRenders.class)
public class MixinRadGeiger {

    @Redirect(method = "addRadiationInfo", at = @At(value = "INVOKE", target = "Lnc/radiation/RadiationHelper;radsPrefix(DZ)Ljava/lang/String;"), remap = false)
    private static String eaglemixins$radsPrefix(double rads, boolean rate) {
        final int n = Math.max(1, nc.config.NCConfig.radiation_unit_prefixes);
        final String num = eagleMixins$formatRads(rads, n);
        return num + " " + (rate ? "Rads/t" : "Rads");
    }

    @Unique
    private static String eagleMixins$formatRads(double v, int n) {
        if (v == 0.0) {
            StringBuilder z = new StringBuilder("0.");
            for (int i = 0; i < n; i++) z.append('0');
            return z.toString();
        }

        double abs = Math.abs(v);
        if (abs < 1.0) {
            return String.format(java.util.Locale.ROOT, "%." + n + "f", v);
        }

        int intDigits = (int) Math.floor(Math.log10(abs)) + 1;
        int decimals = Math.max(0, n - intDigits);
        return String.format(java.util.Locale.ROOT, "%." + decimals + "f", v);
    }
}
