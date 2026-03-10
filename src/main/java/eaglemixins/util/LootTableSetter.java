package eaglemixins.util;

import net.minecraft.util.ResourceLocation;
import java.util.List;

public interface LootTableSetter {

    void eaglemixins$addLootTable(ResourceLocation rl);

    default void eaglemixins$addLootTables(List<ResourceLocation> rls) {
        for (ResourceLocation rl : rls) {
            eaglemixins$addLootTable(rl);
        }
    }
}