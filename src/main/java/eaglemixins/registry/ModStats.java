package eaglemixins.registry;

import net.minecraft.stats.StatBase;
import net.minecraft.util.text.TextComponentTranslation;

public class ModStats {
    public static StatBase GLITCH_COUNT;

    public static void init() {
        GLITCH_COUNT = new StatBase("glitch_count", new TextComponentTranslation("stat.eaglemixins.glitch_count")).registerStat();
    }
}

