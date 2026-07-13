package eaglemixins.mixin.dynamictrees;

import java.util.List;

import com.ferreusveritas.dynamictrees.ModConfigs;
import com.ferreusveritas.dynamictrees.items.Seed;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import eaglemixins.util.dynamictrees.SeasonalGrowthPreview;

/**
 * Adds a growth-speed-by-season tooltip to Dynamic Trees seed items.
 * Deliberately @Inject at RETURN (tail) rather than @Overwrite - appends after
 * the original tooltip content instead of replacing the method, so this can't
 * collide with any other mod injecting into the same method (see the Serene
 * Seasons/Localizator conflict earlier for why this matters).
 *
 * Growth rate in Dynamic Trees depends on the planted biome's rainfall, not
 * the tree species, so this tooltip shows the same generic info on every
 * seed rather than resolving the held item's specific Species. Labeled as
 * "Temperate (Normal)" / "Tropical (Wet)" biomes - bridging DT's own naming
 * with an honest description of what the rainfall > 0.8 check actually
 * measures - with a 3-tier Fast/Moderate/Slow scale per season.
 */
@Mixin(value = Seed.class, remap = false)
public abstract class SeedSeasonTooltipMixin
{
    private static final String[] SEASON_NAMES = { "Spring", "Summer", "Autumn", "Winter" };
    private static final TextFormatting[] SEASON_COLORS = {
            TextFormatting.GREEN, TextFormatting.YELLOW, TextFormatting.GOLD, TextFormatting.AQUA
    };

    @Inject(
            method = "func_77624_a(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Ljava/util/List;Lnet/minecraft/client/util/ITooltipFlag;)V",
            at = @At("RETURN"),
            require = 1
    )
    private void eagleMixins$addSeasonalGrowthTooltip(ItemStack stack, World world, List<String> tooltip, ITooltipFlag flagIn, CallbackInfo ci)
    {
        if (!ModConfigs.enableSeasonalGrowthFactor)
        {
            return;
        }

        tooltip.add(TextFormatting.GRAY + "Growth speed in Dry to Normal biomes:");
        addSpeedLines(tooltip, true);

        tooltip.add(TextFormatting.GRAY + "Growth speed in Humid to Wet biomes:");
        addSpeedLines(tooltip, false);
    }

    private static void addSpeedLines(List<String> tooltip, boolean temperate)
    {
        for (int i = 0; i < 4; i++)
        {
            float factor = temperate ? SeasonalGrowthPreview.temperateFactorAt(i) : SeasonalGrowthPreview.tropicalFactorAt(i);
            String speed = SeasonalGrowthPreview.speedLabel(factor);
            tooltip.add(SEASON_COLORS[i] + " " + SEASON_NAMES[i] + TextFormatting.GRAY + " - " + speed);
        }
    }
}
