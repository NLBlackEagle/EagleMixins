package eaglemixins.mixin.legendarytooltips;

import com.anthonyhilyard.legendarytooltips.LegendaryTooltipsConfig;
import com.anthonyhilyard.legendarytooltips.util.Selectors;
import com.anthonyhilyard.legendarytooltips.util.TextColor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.Configuration;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

@Mixin(LegendaryTooltipsConfig.class)
public class LegendaryTooltipsConfigMixin extends Configuration {

    @Unique
    private final String[] eagleMixins$startColors = new String[64];

    @Unique
    private final String[] eagleMixins$endColors = new String[64];

    @Unique
    private final String[] eagleMixins$bgColors = new String[64];

    @Final
    @Shadow(remap = false)
    private List<List<String>> itemSelectors;

    @Final
    @Shadow(remap = false)
    private List<Integer> framePriorities;

    @Final
    @Shadow(remap = false)
    private transient Map<ItemStack, Integer> frameLevelCache;

    @Shadow(remap = false)
    private Integer getColor(String colorString) {
        colorString = colorString.toLowerCase().replace("0x", "").replace("#", "");
        Integer color = TextColor.parseColor(colorString);
        if (color == null && (colorString.length() == 6 || colorString.length() == 8)) {
            color = TextColor.parseColor("#" + colorString);
        }

        return color;
    }

    @Inject(method = "getCustomBorderStartColor", at = @At("HEAD"), cancellable = true, remap = false)
    private void eaglemixins$extendStartColor(int level, CallbackInfoReturnable<Integer> cir) {
        if (level >= 0 && level < 64 && this.eagleMixins$startColors[level] != null) {
            Integer startColor = this.getColor(this.eagleMixins$startColors[level]);
            cir.setReturnValue(startColor > 0 && startColor <= 16777215 ? startColor | -16777216 : startColor);
        }
    }

    @Inject(method = "getCustomBorderEndColor", at = @At("HEAD"), cancellable = true, remap = false)
    private void eaglemixins$extendEndColor(int level, CallbackInfoReturnable<Integer> cir) {
        if (level >= 0 && level < 64 && this.eagleMixins$endColors[level] != null) {
            Integer endColor = this.getColor(this.eagleMixins$endColors[level]);
            cir.setReturnValue(endColor > 0 && endColor <= 16777215 ? endColor | -16777216 : endColor);
        }
    }

    @Inject(method = "getCustomBackgroundColor", at = @At("HEAD"), cancellable = true, remap = false)
    private void eaglemixins$extendBgColor(int level, CallbackInfoReturnable<Integer> cir) {
        if (level >= 0 && level < 64 && this.eagleMixins$bgColors[level] != null) {
            Integer bgColor = this.getColor(this.eagleMixins$bgColors[level]);
            cir.setReturnValue(bgColor > 0 && bgColor <= 16777215 ? bgColor | -16777216 : bgColor);
        }
    }

    @Inject(method = "getFrameLevelForItem", at = @At("HEAD"), cancellable = true, remap = false)
    private void eaglemixins$expandFrameLevel(ItemStack item, CallbackInfoReturnable<Integer> cir) {
        if (!this.frameLevelCache.containsKey(item)) {
            for (int i = 16; i < 64; ++i) {
                if (i < this.framePriorities.size()) {
                    int frameIndex = this.framePriorities.get(i);
                    if (frameIndex < this.itemSelectors.size()) {
                        for (String entry : this.itemSelectors.get(frameIndex)) {
                            if (Selectors.itemMatches(item, entry)) {
                                this.frameLevelCache.put(item, frameIndex);
                                cir.setReturnValue(frameIndex);
                            }
                        }
                    }
                }
            }
        }
    }


    @Inject(method = "<init>", at = @At("RETURN"))
    private void eaglemixins$expandConfig(File file, CallbackInfo ci) {
        int i;
        for (i = 16; i < 64; ++i) {
            this.itemSelectors.add(Arrays.asList(this.getStringList(String.format("level%d_entries", i), "definitions", new String[0], "")));
        }

        this.eagleMixins$startColors[0] = this.getString("level0_start_color", "colors", "#FF996922", "");
        this.eagleMixins$endColors[0] = this.getString("level0_end_color", "colors", "#FF5A3A1D", "");
        this.eagleMixins$bgColors[0] = this.getString("level0_bg_color", "colors", "#F0160A00", "");

        for (i = 1; i < 64; ++i) {
            this.eagleMixins$startColors[i] = this.getString(String.format("level%d_start_color", i), "colors", "#FF996922", "");
            this.eagleMixins$endColors[i] = this.getString(String.format("level%d_end_color", i), "colors", "#FF5A3A1D", "");
            this.eagleMixins$bgColors[i] = this.getString(String.format("level%d_bg_color", i), "colors", "#F0160A00", "");
        }
    }
}
