package eaglemixins.mixin.qualitytools;

import com.tmtravlr.qualitytools.QualityToolsHelper;
import com.tmtravlr.qualitytools.config.ConfigLoader;
import eaglemixins.util.QualityToolsNBTRemover;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(QualityToolsHelper.class)
public class QualityToolsHelperMixin {

    @Inject(method = "generateQualityTag", at = @At("RETURN"), remap = false)
    private static void onItemMatchesReturn(ItemStack stack, boolean skipNormal, CallbackInfoReturnable<Boolean> cir) {
        if (stack != null && !stack.isEmpty() && (stack.getItem().getItemStackLimit(stack) == 1 || ConfigLoader.allowStackableItems)) {
            QualityToolsNBTRemover.eaglemixins$clean(stack);
        }
    }
}
