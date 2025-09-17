package eaglemixins.mixin.nuclearcraft;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import nc.capability.radiation.source.IRadiationSource;
import nc.config.NCConfig;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(nc.handler.TooltipHandler.class)
public class MixinRadTooltip {

    @ModifyExpressionValue(method = "addRadiationTooltip", at = @At(value = "INVOKE", target="Lnc/radiation/RadiationHelper;getRadiationSource(Lnet/minecraftforge/common/capabilities/ICapabilityProvider;)Lnc/capability/radiation/source/IRadiationSource;"), remap = false)
    private static IRadiationSource eaglemixins$filterLow(IRadiationSource stackRadiation, List<String> tooltip, ItemStack stack) {
        if (stackRadiation != null && stackRadiation.getRadiationLevel() * stack.getCount() <= NCConfig.radiation_lowest_rate) {
            return null;
        }
        return stackRadiation;
    }

    @Redirect(method = "addRadiationTooltip", at = @At(value = "INVOKE", target = "Lnc/radiation/RadiationHelper;radsPrefix(DZ)Ljava/lang/String;"), remap = false)
    private static String eaglemixins$radsPrefix(double rads, boolean rate, List<String> tooltip, ItemStack stack) {
        final int n = Math.max(1, NCConfig.radiation_unit_prefixes);
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
            return String.format(java.util.Locale.ROOT, "%." + n + "f", v); // exactly n decimals
        }
        int intDigits = (int) Math.floor(Math.log10(abs)) + 1;      // digits before decimal
        int decimals  = Math.max(0, n - intDigits);                 // keep total sig-digits = n
        return String.format(java.util.Locale.ROOT, "%." + decimals + "f", v);
    }
}