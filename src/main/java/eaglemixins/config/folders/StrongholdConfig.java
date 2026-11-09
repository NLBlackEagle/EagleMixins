package eaglemixins.config.folders;

import net.minecraftforge.common.config.Config;

public class StrongholdConfig {

    @Config.Comment({
            "Pushes every stronghold ring this many blocks further away from spawn, keeping the normal ring layout beyond it.",
            "With vanilla ring settings the first ring then sits at about this + 1,400 to 2,700 blocks.",
            "Only affects newly generated worlds/dimensions: existing strongholds stay where they are, but eyes of ender use the new positions."
    })
    @Config.Name("Ring Offset (blocks)")
    @Config.RangeInt(min = 0)
    public int ringOffset = 10000;

    @Config.Comment({
            "Strongholds in the first ring. Each next ring gets more (vanilla: 3, 6, 10, 15, 21, ...).",
            "Since the rings are pushed outward they are a lot bigger, so they need more strongholds to not be far apart.",
            "0 = keep the world's own value (vanilla 3, or StrongholdSpread in the OTG WorldConfig)."
    })
    @Config.Name("Strongholds In First Ring")
    @Config.RangeInt(min = 0)
    public int spread = 12;

    @Config.Comment({
            "Total amount of strongholds in the world. Once they run out, no further rings are made.",
            "With the defaults (offset 10000, 12 in the first ring) 256 fills rings from ~12,000 to ~27,000 blocks.",
            "Higher values reach further out but make the first stronghold lookup (eye of ender, worldgen) slower.",
            "0 = keep the world's own value (vanilla 128, or StrongholdCount in the OTG WorldConfig)."
    })
    @Config.Name("Stronghold Count")
    @Config.RangeInt(min = 0)
    public int count = 256;
}
