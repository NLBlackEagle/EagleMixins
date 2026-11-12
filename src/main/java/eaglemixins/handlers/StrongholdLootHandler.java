package eaglemixins.handlers;

import eaglemixins.config.ForgeConfigHandler;
import eaglemixins.config.folders.BetterStrongholdConfig;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootEntry;
import net.minecraft.world.storage.loot.LootEntryTable;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.RandomValueRange;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Adds a pool to the stronghold chest tables (the ones set in the Better Stronghold config) that rolls whole other tables,
 * by default minecraft:chests/simple_dungeon. A LootEntryTable looks its table up when the loot is generated, so everything
 * other mods injected into that table (LootTableLoadEvent) is part of it.
 */
public class StrongholdLootHandler {

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onLootTableLoad(LootTableLoadEvent event) {
        BetterStrongholdConfig cfg = ForgeConfigHandler.betterstronghold;
        if (cfg.extraLootRolls <= 0 || cfg.extraLootTables.length == 0) return;
        if (!isStrongholdTable(event.getName(), cfg)) return;

        for (String extra : cfg.extraLootTables) {
            if (extra == null || extra.trim().isEmpty()) continue;
            ResourceLocation extraTable = new ResourceLocation(extra.trim());
            if (extraTable.equals(event.getName())) continue; // a table can't roll itself

            String name = "eaglemixins_extra_" + extraTable.toString().replace(':', '_').replace('/', '_');
            if (event.getTable().getPool(name) != null) continue;
            LootEntry entry = new LootEntryTable(extraTable, 1, 0, new LootCondition[0], name);
            event.getTable().addPool(new LootPool(new LootEntry[]{entry}, new LootCondition[0],
                    new RandomValueRange(cfg.extraLootRolls), new RandomValueRange(0), name));
        }
    }

    private static boolean isStrongholdTable(ResourceLocation name, BetterStrongholdConfig cfg) {
        return matches(name, cfg.corridorLootTable) || matches(name, cfg.crossingLootTable) || matches(name, cfg.libraryLootTable);
    }

    private static boolean matches(ResourceLocation name, String table) {
        return table != null && !table.trim().isEmpty() && name.equals(new ResourceLocation(table.trim()));
    }
}
