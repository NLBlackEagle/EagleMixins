package eaglemixins.mixin.legendarytooltips;

import com.anthonyhilyard.legendarytooltips.LegendaryTooltips;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LegendaryTooltips.class)
public interface LegendaryTooltipsAccessor {
    @Invoker(value = "itemFrameColors", remap = false)
    static Integer[] callItemFrameColors(ItemStack stack, Integer[] defaults) {
        throw new AssertionError(); // Will be replaced by Mixin
    }
}
