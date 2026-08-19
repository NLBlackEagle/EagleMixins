package eaglemixins.mixin.qualitytools;

import com.tmtravlr.qualitytools.QualityToolsHelper;
import eaglemixins.util.LootGenerationContext;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(QualityToolsHelper.class)
public class QualityToolsHelperMixin {

    @Inject(method = "generateQualityTag", at = @At("RETURN"), remap = false)
    private static void eaglemixins_removeSavedTables(ItemStack stack, boolean skipNormal, CallbackInfoReturnable<Boolean> cir) {
        LootGenerationContext.removeTableNBT(stack);
    }
}
