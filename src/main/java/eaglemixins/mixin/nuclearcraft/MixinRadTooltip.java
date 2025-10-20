package eaglemixins.mixin.nuclearcraft;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import eaglemixins.util.RadsFormatter;
import nc.capability.radiation.source.IRadiationSource;
import nc.config.NCConfig;
import nc.handler.TooltipHandler;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(TooltipHandler.class)
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
        final String num = RadsFormatter.formatRads(rads, n);
        return num + " " + (rate ? "Rads/t" : "Rads");
    }
}