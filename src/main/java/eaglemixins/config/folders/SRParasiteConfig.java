package eaglemixins.config.folders;

import net.minecraftforge.common.config.Config;

import java.util.ArrayList;
import java.util.Arrays;

public class SRParasiteConfig {

    @Config.Comment("Treat the allowed biomes list as whitelist. Set to false to treat as blacklist")
    @Config.Name("SRParasites allowed biomes is whitelist")
    public boolean biomeListIsWhitelist = true;

    //Parasites will be allowed to spawn via spawners, stay alive and will drop (reduced) loot in these biomes
    @Config.Comment("List of biome IDs to whitelist or blacklist depending on biomeListIsWhitelist.\n" +
            "This list supports the * wildcard, example: biomesoplenty:* would whitelist all biomesoplenty biomes.\n" +
            "Optionally prefix an entry with \"dimensionId@\" to restrict it to a specific dimension,\n" +
            "example: 1@biomesoplenty:steppe only matches biomesoplenty:steppe in dimension 1.\n" +
            "Entries with no dimension prefix (or a \"*@\" prefix) match that biome in any dimension.\n" +
            "The dimension prefix can be combined with the wildcard biome, example: 1@biomesoplenty:*"
    )
    @Config.Name("SRParasites allowed biomes")
    public ArrayList<String> biomeList = new ArrayList<>(Arrays.asList(
            "0@biomesoplenty:steppe",
            "0@openterraingenerator:overworld_abyssal_rift",
            "0@srparasites:biome_parasite",
            "0@openterraingenerator:overworld_lair_of_the_thing",
            "0@openterraingenerator:overworld_nuclear_ruins",
            "0@openterraingenerator:overworld_ruins_of_blight",
            "1@*",
            "3@*",
            "-1@*",
            "111@*"
    ));

    @Config.Comment({
            "EagleMixins modifies parasite loot in the overworld.",
            "Parasites are only allowed to drop loot in certain named biomes (\"SRParasites allowed biomes\")",
            "In those biomes, they have a reduced chance to drop parasite loot and will drop Corrupted Ashes instead (\"Corrupted Ashes chance\")",
            "Only parasites with specific custom names are exempt from this modification (\"Parasite full loot-drop enabler\")",
            "If set to false, EagleMixins will instead not modify parasite loot at all."
    })
    @Config.Name("Modify Parasite Loot")
    public boolean modifyLoot = true;

    @Config.Comment({
            "Parasite display names that are allowed to always drop loot (even outside allowed biomes).",
            "This matches the entity's *custom display name*, not its ID.",
            "It's enough if the custom name contains any of these listed strings for it to be always allowed to drop loot.",
            "Requires 'Modify Parasite Loot' to be enabled."
    })
    @Config.Name("Parasite full loot-drop enabler")
    public ArrayList<String> keepLootNames = new ArrayList<>(Arrays.asList(
            "Sentient Horror",
            "Degrading Overseer",
            "Malformed Observer",
            "Shivaxi",
            "Corrupted Carrier",
            "Necrotic Blight"
    ));

    @Config.Comment({
            "Minimum distance in blocks from a Stage IV Beckon at which it spawns new Beckons.",
            "Requires the \"Stage IV Beckon Spread (SRP)\" mixin toggle."
    })
    @Config.Name("Stage IV Beckon Spread min range")
    @Config.RangeInt(min = 0, max = 64)
    public int beckonSpreadMinRange = 3;

    @Config.Comment({
            "Maximum distance in blocks from a Stage IV Beckon at which it spawns new Beckons.",
            "Requires the \"Stage IV Beckon Spread (SRP)\" mixin toggle."
    })
    @Config.Name("Stage IV Beckon Spread max range")
    @Config.RangeInt(min = 0, max = 64)
    public int beckonSpreadMaxRange = 6;

    @Config.Comment({
            "A Stage IV Beckon will not spawn another Beckon if this many Beckons are already within the max range around it (not counting itself).",
            "Requires the \"Stage IV Beckon Spread (SRP)\" mixin toggle."
    })
    @Config.Name("Stage IV Beckon Spread max nearby Beckons")
    @Config.RangeInt(min = 0)
    public int beckonSpreadMaxNearby = 3;

    @Config.Comment({
            "SRP prevents Beckons spawned by a Stage IV Beckon from ever growing. Set to true to allow them to grow.",
            "Requires the \"Stage IV Beckon Spread (SRP)\" mixin toggle."
    })
    @Config.Name("Stage IV Beckon Spread Beckons can grow")
    public boolean beckonSpreadCanGrow = false;

    @Config.Comment("All SRParasites outside of the allowed biomes in the allowed biome whitelist will automatically be killed")
    @Config.Name("Kill Parasites outside alllowed biomes")
    public boolean killEscapedParasites = true;

    @Config.Comment("Parasite drops in the overworld have a chance to instead drop as corrupted ashes. This is the chance for that to happen.")
    @Config.Name("Corrupted Ashes chance")
    public float chanceCorruptedAshes = 0.375f;
}