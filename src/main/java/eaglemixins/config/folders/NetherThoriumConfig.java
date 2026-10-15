package eaglemixins.config.folders;

import net.minecraftforge.common.config.Config;

public class NetherThoriumConfig {
    @Config.Comment("Generate thorium ore in netherrack")
    @Config.Name("Enabled")
    public boolean enabled = true;

    @Config.Comment("Dimensions the ore generates in. It only replaces netherrack")
    @Config.Name("Dimensions")
    public int[] dimensions = {-1};

    @Config.Comment("Max amount of ore blocks per vein (vanilla diamond: 8)")
    @Config.Name("Vein Size")
    @Config.RangeInt(min = 1, max = 64)
    public int veinSize = 8;

    @Config.Comment("Veins attempted per chunk (vanilla diamond: 1)")
    @Config.Name("Veins Per Chunk")
    @Config.RangeInt(min = 0, max = 64)
    public int veinsPerChunk = 1;

    @Config.Comment("Chance per chunk that any veins get attempted at all. Lower to make it rarer than one vein per chunk (1.0 = every chunk)")
    @Config.Name("Chunk Chance")
    @Config.RangeDouble(min = 0, max = 1)
    public double chunkChance = 1.0;

    @Config.Comment("Lowest Y level a vein can start at")
    @Config.Name("Min Height")
    @Config.RangeInt(min = 0, max = 255)
    public int minHeight = 1;

    @Config.Comment("Highest Y level a vein can start at")
    @Config.Name("Max Height")
    @Config.RangeInt(min = 0, max = 255)
    public int maxHeight = 126;
}
