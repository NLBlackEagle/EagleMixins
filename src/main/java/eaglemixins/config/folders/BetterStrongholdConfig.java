package eaglemixins.config.folders;

import eaglemixins.EagleMixins;
import net.minecraft.entity.EntityList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.WeightedRandom;
import net.minecraftforge.common.config.Config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class BetterStrongholdConfig {

    @Config.Comment({
            "Stronghold pieces (except the center portal room) don't place blocks where the world is air,",
            "so they are cut off by caves, ravines and open sky instead of floating in them."
    })
    @Config.Name("Cut Off Stronghold Pieces Into Air")
    public boolean cutOffIntoAir = true;

    @Config.Comment({
            "Before the first piece of a new stronghold generates, checks if the chunk of the center portal room is solid underground.",
            "If not, the same check is done this many chunks away: first north/east/south/west, then the 4 corners (8 checks at most).",
            "The whole stronghold moves to the first solid spot, if none is solid it stays where it is.",
            "Chunks that don't exist yet get generated without being saved just to check them (terrain and caves only).",
            "0 = disabled. Only new strongholds are moved, eyes of ender and /locate point to the moved stronghold."
    })
    @Config.Name("Center Room Relocation Distance (chunks)")
    @Config.RangeInt(min = 0, max = 8)
    public int relocationDistance = 3;

    @Config.Comment({
            "Fraction (0-1) of the sampled blocks in the center room's chunk, at the height of the room, that has to be solid",
            "for the spot to count as solid ground. Air, liquids and plants don't count as solid."
    })
    @Config.Name("Center Room Min Solid Fraction")
    @Config.RangeDouble(min = 0.0D, max = 1.0D)
    public double minSolidFraction = 0.9D;

    @Config.Comment({
            "Loot table for chests in corridors, dead ends and the portal room.",
            "Empty = keep the mod's own table (bt:stronghold_c)."
    })
    @Config.Name("Corridor Chest Loot Table")
    public String corridorLootTable = "minecraft:chests/stronghold_corridor";

    @Config.Comment({
            "Loot table for chests in crossings (cross_1 to cross_4 and the second level crossing).",
            "Empty = keep the mod's own table (bt:stronghold_c)."
    })
    @Config.Name("Crossing Chest Loot Table")
    public String crossingLootTable = "minecraft:chests/stronghold_crossing";

    @Config.Comment({
            "Loot table for chests in the library.",
            "Empty = keep the mod's own table (bt:stronghold_c)."
    })
    @Config.Name("Library Chest Loot Table")
    public String libraryLootTable = "minecraft:chests/stronghold_library";

    @Config.Comment({
            "Loot tables rolled on top of the stronghold chest tables above (corridor, crossing, library), so stronghold chests",
            "also get e.g. a full simple dungeon roll. The table is looked up when the chest is opened, so modded items that other",
            "mods inject into it come along. Applies to every chest using those tables, also vanilla/OTG strongholds. Empty = off.",
            "Takes effect when the world loads its loot tables (world (re)load)."
    })
    @Config.Name("Extra Chest Loot Tables")
    public String[] extraLootTables = {"minecraft:chests/simple_dungeon"};

    @Config.Comment("How often each extra loot table is rolled per stronghold chest")
    @Config.Name("Extra Chest Loot Rolls")
    @Config.RangeInt(min = 0, max = 10)
    public int extraLootRolls = 1;

    @Config.Comment({
            "Entities the stronghold spawners can get, format: <entity id>, <weight>",
            "Each spawner that generates picks one entity from this list by weight. Unknown entity ids are skipped with a warning.",
            "Empty list = keep the mod's own spawners (zombie, skeleton, cave spider).",
            "Whether spawners generate at all is still up to the BetterStronghold config (\"Stronghold Mob Additions\", 1 in 3 gets removed)."
    })
    @Config.Name("Spawner Entities")
    public String[] spawnerEntities = {
            "minecraft:zombie, 10",
            "minecraft:skeleton, 10",
            "minecraft:silverfish, 6",
            "minecraft:cave_spider, 6",
            "minecraft:spider, 5",
            "minecraft:zombie_villager, 4",
            "minecraft:husk, 3",
            "minecraft:stray, 3",
            "minecraft:creeper, 3",
            "minecraft:witch, 3",
            "minecraft:endermite, 3",
            "minecraft:vindication_illager, 2",
            "minecraft:wither_skeleton, 2",
            "minecraft:evocation_illager, 1",
            "mod_lavacow:unburied, 5",
            "mod_lavacow:forsaken, 3",
            "mod_lavacow:mummy, 2",
            "mod_lavacow:banshee, 2",
            "mod_lavacow:undertaker, 1",
            "lycanitesmobs:ghoul, 3",
            "lycanitesmobs:geist, 3",
            "lycanitesmobs:darkling, 3",
            "lycanitesmobs:trite, 3",
            "lycanitesmobs:cryptkeeper, 2",
            "lycanitesmobs:grue, 2",
            "lycanitesmobs:tarantula, 2",
            "lycanitesmobs:banshee, 1",
            "lycanitesmobs:shade, 1",
            "iceandfire:dread_thrall, 3",
            "iceandfire:dread_ghoul, 3",
            "iceandfire:ghost, 1",
            "iceandfire:dread_knight, 1"
    };

    @Config.Comment({
            "false: every spawner spawns a single entity type picked from \"Spawner Entities\".",
            "true: every spawner can spawn the whole \"Spawner Entities\" list, picking by weight on every spawn."
    })
    @Config.Name("Mixed Spawners")
    public boolean mixedSpawners = false;

    private List<SpawnerEntry> parsedSpawnerEntities = null;

    public void reset() {
        parsedSpawnerEntities = null;
    }

    public List<SpawnerEntry> getSpawnerEntities() {
        if (parsedSpawnerEntities == null) {
            List<SpawnerEntry> list = new ArrayList<>();
            for (String line : spawnerEntities) {
                String[] parts = line.split(",");
                if (parts[0].trim().isEmpty()) continue;
                ResourceLocation id = new ResourceLocation(parts[0].trim());
                int weight = 1;
                try {
                    if (parts.length > 1) weight = Integer.parseInt(parts[1].trim());
                } catch (NumberFormatException e) {
                    EagleMixins.LOGGER.warn("Better Stronghold: invalid weight in spawner entity \"{}\"", line);
                    continue;
                }
                if (weight <= 0) continue;
                if (!EntityList.isRegistered(id)) {
                    EagleMixins.LOGGER.warn("Better Stronghold: unknown spawner entity \"{}\", skipping", id);
                    continue;
                }
                list.add(new SpawnerEntry(id, weight));
            }
            parsedSpawnerEntities = Collections.unmodifiableList(list);
        }
        return parsedSpawnerEntities;
    }

    public SpawnerEntry pickSpawnerEntity(Random rand) {
        List<SpawnerEntry> list = getSpawnerEntities();
        return list.isEmpty() ? null : WeightedRandom.getRandomItem(rand, list);
    }

    public static class SpawnerEntry extends WeightedRandom.Item {
        public final ResourceLocation id;

        public SpawnerEntry(ResourceLocation id, int weight) {
            super(weight);
            this.id = id;
        }
    }
}
