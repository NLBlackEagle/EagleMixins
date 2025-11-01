package eaglemixins.mixin.legendarytooltips;

import com.anthonyhilyard.legendarytooltips.LegendaryTooltipsConfig;
import net.minecraftforge.common.config.Configuration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(LegendaryTooltipsConfig.class)
public class LegendaryTooltipsConfigMixin extends Configuration {
    @ModifyConstant(method = {"getCustomBorderStartColor", "getCustomBorderEndColor", "getCustomBackgroundColor"}, constant = @Constant(intValue = 15), remap = false)
    private int eagleMixins_modifyMaxIndex_63(int constant){
        return 63;
    }

    @ModifyConstant(method = {"<init>", "getFrameLevelForItem"}, constant = @Constant(intValue = 16),remap = false)
    private int eagleMixins_modifyMaxIndex_64(int constant){
        return 64;
    }
}
