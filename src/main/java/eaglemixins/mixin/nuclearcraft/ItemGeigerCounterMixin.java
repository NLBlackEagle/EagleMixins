package eaglemixins.mixin.nuclearcraft;

import eaglemixins.util.RadsFormatter;
import nc.item.bauble.ItemGeigerCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ItemGeigerCounter.class)
public abstract class ItemGeigerCounterMixin {

    @Redirect(
            method = "onItemRightClick",
            at = @At(value = "INVOKE", target = "Lnc/radiation/RadiationHelper;radsPrefix(DZ)Ljava/lang/String;", remap = false)
    )
    private String eaglemixins$radsPrefix(double rads, boolean rate) {
        final int n = Math.max(1, nc.config.NCConfig.radiation_unit_prefixes);
        final String num = RadsFormatter.formatRads(rads, n);
        return num + " " + (rate ? "Rads/t" : "Rads");
    }
}
