package eaglemixins.mixin.nuclearcraft;

import eaglemixins.util.RadsFormatter;
import nc.block.tile.radiation.BlockGeigerCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BlockGeigerCounter.class)
public abstract class BlockGeigerCounterMixin {

    @Redirect(
            method = "onBlockActivated",
            at = @At(value = "INVOKE", target = "Lnc/radiation/RadiationHelper;radsPrefix(DZ)Ljava/lang/String;", remap = false)
    )
    private String eaglemixins_radsPrefix(double rads, boolean rate) {
        final int n = Math.max(1, nc.config.NCConfig.radiation_unit_prefixes);
        final String num = RadsFormatter.formatRads(rads, n);
        return num + " " + (rate ? "Rads/t" : "Rads");
    }
}
