package eaglemixins.mixin.legendarytooltips;

import com.anthonyhilyard.legendarytooltips.LegendaryTooltips;
import com.anthonyhilyard.legendarytooltips.LegendaryTooltipsConfig;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LegendaryTooltips.class)
public abstract class LegendaryTooltipsMixin {


    @Inject(method = "itemFrameColors", at = @At("HEAD"), cancellable = true, remap = false)
    private static void eagleMixins_itemFrameColors(ItemStack stack, Integer[] defaults, CallbackInfoReturnable<Integer[]> cir) {
        if (stack == null || stack.isEmpty()) {
            cir.setReturnValue(defaults);
            return;
        }

        if (LegendaryTooltipsConfig.INSTANCE == null) {
            cir.setReturnValue(defaults);
            return;
        }

        try {
            int frameLevel = LegendaryTooltipsConfig.INSTANCE.getFrameLevelForItem(stack);
            // Allow original method to continue
        } catch (Throwable t) {
            System.err.println("[LegendaryTooltipsMixin] Exception in getFrameLevelForItem:");
            t.printStackTrace();
            cir.setReturnValue(defaults);
        }
    }
}