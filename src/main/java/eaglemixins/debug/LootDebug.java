package eaglemixins.debug;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootTable;

import java.util.Map;
import java.util.WeakHashMap;

public final class LootDebug {
    public static final Map<LootTable, ResourceLocation> LT_IDS = new WeakHashMap<>();
}