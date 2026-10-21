package eaglemixins.config.folders;

import net.minecraftforge.common.config.Config;

public class OTGBiomeSearchConfig {

    @Config.Comment("Search radius in blocks used by /otg tp <biome> when no radius is given. OTG's own search used 8000.")
    @Config.Name("Default Search Radius")
    @Config.RangeInt(min = 64, max = 1000000)
    public int defaultRadius = 8000;

    @Config.Comment({
            "Largest radius in blocks that can be requested with /otg tp <biome> <radius>. Bigger requests are clamped to this.",
            "Search memory use does not depend on the radius, only the search time does (it grows with radius squared).",
            "Rough worst case (biome not found): 50000 blocks is ~4x the time of 25000, 100000 blocks is ~4x the time of 50000."
    })
    @Config.Name("Maximum Search Radius")
    @Config.RangeInt(min = 64, max = 1000000)
    public int maxRadius = 50000;

    @Config.Comment({
            "Milliseconds per server tick spent searching (shared by all running searches).",
            "Higher finishes searches sooner but costs more TPS while a search is running. A tick is 50ms in total."
    })
    @Config.Name("Search Time Budget Per Tick (ms)")
    @Config.RangeInt(min = 1, max = 45)
    public int tickBudgetMs = 10;

    @Config.Comment("Seconds between \"still searching\" progress messages. 0 disables them.")
    @Config.Name("Progress Message Interval (s)")
    @Config.RangeInt(min = 0)
    public int progressIntervalSeconds = 5;
}
