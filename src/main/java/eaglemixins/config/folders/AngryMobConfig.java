package eaglemixins.config.folders;

import eaglemixins.EagleMixins;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Config;

import java.util.HashMap;
import java.util.Map;

public class AngryMobConfig {

    public static final ResourceLocation WOLF = new ResourceLocation("minecraft", "wolf");

    @Config.Comment({
            "Gives untamed wolves a chance to turn hostile toward a player they see.",
            "When an eligible wolf sees a player it rolls 'chance' once for that sighting; on a hit it becomes",
            "an angry wolf and hunts that player until the player leaves its line of sight, then calms down.",
            "A miss is rolled again the next time a player comes into view.",
            "Syntax: entityid, baby, adult, chance",
            "  entityid - currently only minecraft:wolf is supported; other entries are logged and ignored.",
            "  baby     - true to also affect baby wolves.",
            "  adult    - true to affect adult wolves.",
            "  chance   - per-sighting probability, 0.0 to 1.0. 0 disables the feature.",
            "Example: minecraft:wolf, false, true, 0.2"
    })
    @Config.Name("Angry Mob Rules")
    public String[] angryMobs = { "minecraft:wolf, false, true, 0.0" };

    @Config.Comment("Ticks a wolf keeps hunting after losing line of sight of its target before it calms down (20 ticks = 1 second).")
    @Config.Name("Lose Sight Grace Ticks")
    @Config.RangeInt(min = 0)
    public int loseSightTicks = 60;

    public static final class Entry {
        public final boolean baby;
        public final boolean adult;
        public final float chance;

        Entry(boolean baby, boolean adult, float chance) {
            this.baby = baby;
            this.adult = adult;
            this.chance = chance;
        }
    }

    private final Map<ResourceLocation, Entry> entries = new HashMap<>();
    private boolean parsed = false;

    public Entry getEntry(ResourceLocation id) {
        if (!parsed) parse();
        return entries.get(id);
    }

    private void parse() {
        parsed = true;
        for (String raw : angryMobs) {
            String[] s = raw.split(",");
            if (s.length < 4) {
                EagleMixins.LOGGER.warn("Failed parsing angry mob rule ({}) - expected 4 comma-separated fields", raw);
                continue;
            }
            try {
                ResourceLocation id = new ResourceLocation(s[0].trim());
                if (!id.equals(WOLF)) {
                    EagleMixins.LOGGER.warn("Angry mob rule ({}) - only minecraft:wolf is supported, ignoring", raw);
                    continue;
                }
                boolean baby = Boolean.parseBoolean(s[1].trim());
                boolean adult = Boolean.parseBoolean(s[2].trim());
                float chance = Float.parseFloat(s[3].trim());
                if (chance < 0f || chance > 1f) {
                    EagleMixins.LOGGER.warn("Angry mob rule ({}) - chance must be between 0.0 and 1.0, ignoring", raw);
                    continue;
                }
                entries.put(id, new Entry(baby, adult, chance));
            } catch (Exception ex) {
                EagleMixins.LOGGER.warn("Failed parsing angry mob rule ({})", raw);
            }
        }
    }

    public void reset() {
        entries.clear();
        parsed = false;
    }
}