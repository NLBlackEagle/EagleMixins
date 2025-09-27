
package eaglemixins.util;

import javax.annotation.Nullable;
import net.minecraft.util.ResourceLocation;

public interface LootTableSetter {
    void eaglemixins$setLootTable(@Nullable ResourceLocation rl);
}