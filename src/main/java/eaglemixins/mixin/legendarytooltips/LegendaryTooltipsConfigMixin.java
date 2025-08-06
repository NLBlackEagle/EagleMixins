package eaglemixins.mixin.legendarytooltips;

import com.anthonyhilyard.legendarytooltips.LegendaryTooltipsConfig;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;
import java.lang.reflect.Field;
import java.util.*;

@Mixin(LegendaryTooltipsConfig.class)
public class LegendaryTooltipsConfigMixin {

    @Inject(method = "getCustomBorderStartColor", at = @At("HEAD"), cancellable = true, remap = false)
    private void eaglemixins$guardStartColor(int level, CallbackInfoReturnable<Integer> cir) {
        if (level >= 64) {
            cir.setReturnValue(-1);
        }
    }

    @Inject(method = "getCustomBorderEndColor", at = @At("HEAD"), cancellable = true, remap = false)
    private void eaglemixins$guardEndColor(int level, CallbackInfoReturnable<Integer> cir) {
        if (level >= 64) {
            cir.setReturnValue(-1);
        }
    }

    @Inject(method = "getCustomBackgroundColor", at = @At("HEAD"), cancellable = true, remap = false)
    private void eaglemixins$guardBgColor(int level, CallbackInfoReturnable<Integer> cir) {
        if (level >= 64) {
            cir.setReturnValue(-1);
        }
    }

    @Inject(method = "getFrameLevelForItem", at = @At("RETURN"), cancellable = true, remap = false)
    private void eaglemixins$debugFrameLevel(ItemStack item, CallbackInfoReturnable<Integer> cir) {
        int result = cir.getReturnValue();

        // Debug log to check what's going on with frame levels 16 and 17+
        if ((result >= 16) && (result < 17)) {
            System.out.println("[LegendaryTooltipsConfigMixin] Frame level = " + result + " for item: " + item.getItem().getRegistryName() + " | Damage = " + item.getItemDamage());
        }
    }


    @Inject(method = "<init>", at = @At("RETURN"))
    private void eaglemixins$expandConfig(File file, CallbackInfo ci) {
        try {
            LegendaryTooltipsConfig instance = (LegendaryTooltipsConfig)(Object)this;
            if (instance == null) {
                System.out.println("[LegendaryTooltipsConfigMixin] ERROR: instance is null.");
                return;
            }

            // === Expand itemSelectors ===
            try {
                Field selectorsField = LegendaryTooltipsConfig.class.getDeclaredField("itemSelectors");
                selectorsField.setAccessible(true);
                List<List<String>> selectors = (List<List<String>>) selectorsField.get(instance);
                System.out.println("[LegendaryTooltipsConfigMixin] itemSelectors (initial size): " + selectors.size());

                while (selectors.size() < 64) selectors.add(new ArrayList<>());
                System.out.println("[LegendaryTooltipsConfigMixin] itemSelectors extended to size: " + selectors.size());
            } catch (Exception ex) {
                System.out.println("[LegendaryTooltipsConfigMixin] ERROR in itemSelectors:");
                ex.printStackTrace();
            }

            // === Expand framePriorities ===
            try {
                Field prioritiesField = LegendaryTooltipsConfig.class.getDeclaredField("framePriorities");
                prioritiesField.setAccessible(true);
                List<Integer> priorities = (List<Integer>) prioritiesField.get(instance);
                System.out.println("[LegendaryTooltipsConfigMixin] framePriorities (initial size): " + priorities.size());

                for (int i = priorities.size(); i < 64; i++) priorities.add(i);
                System.out.println("[LegendaryTooltipsConfigMixin] framePriorities extended to size: " + priorities.size());
            } catch (Exception ex) {
                System.out.println("[LegendaryTooltipsConfigMixin] ERROR in framePriorities:");
                ex.printStackTrace();
            }

            // === Expand color arrays ===
            try {
                Field startColorsField = LegendaryTooltipsConfig.class.getDeclaredField("startColors");
                Field endColorsField = LegendaryTooltipsConfig.class.getDeclaredField("endColors");
                Field bgColorsField = LegendaryTooltipsConfig.class.getDeclaredField("bgColors");

                startColorsField.setAccessible(true);
                endColorsField.setAccessible(true);
                bgColorsField.setAccessible(true);

                String[] startColors = Arrays.copyOf((String[]) startColorsField.get(instance), 64);
                String[] endColors   = Arrays.copyOf((String[]) endColorsField.get(instance), 64);
                String[] bgColors    = Arrays.copyOf((String[]) bgColorsField.get(instance), 64);

                System.out.println("[LegendaryTooltipsConfigMixin] Color arrays extended to size 64.");

                Field selectorsField = LegendaryTooltipsConfig.class.getDeclaredField("itemSelectors");
                selectorsField.setAccessible(true);
                List<List<String>> selectors = (List<List<String>>) selectorsField.get(instance);

                for (int i = 16; i < 64; i++) {
                    startColors[i] = instance.getString(String.format("level%d_start_color", i), "colors", "#FF996922", "");
                    endColors[i]   = instance.getString(String.format("level%d_end_color", i), "colors", "#FF5A3A1D", "");
                    bgColors[i]    = instance.getString(String.format("level%d_bg_color", i), "colors", "#F0160A00", "");

                    String[] entries = instance.getStringList(String.format("level%d_entries", i), "definitions", new String[0], "");
                    selectors.set(i, Arrays.asList(entries));
                }

                startColorsField.set(instance, startColors);
                endColorsField.set(instance, endColors);
                bgColorsField.set(instance, bgColors);

                System.out.println("[LegendaryTooltipsConfigMixin] Color values and selectors set for levels 16–63.");
            } catch (Exception ex) {
                System.out.println("[LegendaryTooltipsConfigMixin] ERROR in color arrays or entry patching:");
                ex.printStackTrace();
            }

        } catch (Exception outer) {
            System.out.println("[LegendaryTooltipsConfigMixin] CRITICAL failure:");
            outer.printStackTrace();
        }
    }
}
