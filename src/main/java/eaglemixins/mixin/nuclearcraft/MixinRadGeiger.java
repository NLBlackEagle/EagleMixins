package eaglemixins.mixin.nuclearcraft;

import eaglemixins.util.RadsFormatter;
import nc.radiation.RadiationRenders;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RadiationRenders.class)
public abstract class MixinRadGeiger {

    @Redirect(method = "addRadiationInfo", at = @At(value = "INVOKE", target = "Lnc/radiation/RadiationHelper;radsPrefix(DZ)Ljava/lang/String;"), remap = false)
    private String eaglemixins_radsPrefix(double rads, boolean rate) {
        final int n = Math.max(1, nc.config.NCConfig.radiation_unit_prefixes);
        final String num = RadsFormatter.formatRads(rads, n);
        return num + " " + (rate ? "Rads/t" : "Rads");
    }
}
