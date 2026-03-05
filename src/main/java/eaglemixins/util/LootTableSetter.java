package eaglemixins.util;

import net.minecraft.util.ResourceLocation;
import java.util.List;

public interface LootTableSetter {

    void eaglemixins$setLootTable(ResourceLocation rl); // single
    void eaglemixins$setLootTables(List<ResourceLocation> rls); // multiple
}