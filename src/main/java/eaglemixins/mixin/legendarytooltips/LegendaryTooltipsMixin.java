package eaglemixins.mixin.legendarytooltips;

import com.anthonyhilyard.legendarytooltips.LegendaryTooltips;
import com.anthonyhilyard.legendarytooltips.LegendaryTooltipsConfig;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;

@Mixin(LegendaryTooltips.class)
public abstract class LegendaryTooltipsMixin {


    @Inject(method = "itemFrameColors", at = @At("HEAD"), cancellable = true, remap = false)
    private static void eagleMixins$itemFrameColors(ItemStack stack, Integer[] defaults, CallbackInfoReturnable<Integer[]> cir) {
        System.out.println("[LegendaryTooltipsMixin] Called itemFrameColors()");
        System.out.println("[LegendaryTooltipsMixin] Item returns {}" + stack);
        System.out.println("[LegendaryTooltipsMixin] Defaults: " + Arrays.toString(defaults));
        if (stack == null || stack.isEmpty()) {
            System.out.println("[LegendaryTooltipsMixin] Stack is null or empty.");
            cir.setReturnValue(defaults);
            return;
        }

        if (LegendaryTooltipsConfig.INSTANCE == null) {
            System.out.println("[LegendaryTooltipsMixin] Config instance is null.");
            cir.setReturnValue(defaults);
            return;
        }

        try {
            int frameLevel = LegendaryTooltipsConfig.INSTANCE.getFrameLevelForItem(stack);
            System.out.println("[LegendaryTooltipsMixin] Frame level = " + frameLevel);
            // Allow original method to continue
        } catch (Throwable t) {
            System.err.println("[LegendaryTooltipsMixin] Exception in getFrameLevelForItem:");
            t.printStackTrace();
            cir.setReturnValue(defaults);
        }
    }
}