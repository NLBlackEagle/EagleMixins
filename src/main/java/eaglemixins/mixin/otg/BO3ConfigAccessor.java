package eaglemixins.mixin.otg;

import com.pg85.otg.customobjects.bo3.BO3Config;
import com.pg85.otg.util.materials.MaterialSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BO3Config.class)
public interface BO3ConfigAccessor {
    @Accessor(value = "sourceBlocks", remap = false)
    MaterialSet getSourceBlocks();
}
