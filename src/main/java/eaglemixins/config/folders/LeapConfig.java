package eaglemixins.config.folders;

import eaglemixins.EagleMixins;
import eaglemixins.entity.ai.LeapSettings;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Config;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class LeapConfig {

    @Config.Comment("Master toggle. If false, no mob gets the leap AI from this config or from NBT.")
    @Config.Name("Enabled")
    public boolean enabled = true;

    @Config.Comment({
            "Settings every leap rule starts from, so a rule only needs the settings it changes. Also used by mobs that only have the NBT flag.",
            "  minRange      Min horizontal distance to the target (blocks) to leap",
            "  maxRange      Max horizontal distance to the target (blocks) to leap",
            "  minHeight     Target must be at least this many blocks above the mob. 2 = only at players who pillared up, -64 = any height",
            "  distance      [range] Leap distance compared to the target's distance. 1 = lands on the target, 0.8 = 20% short, 1.2 = 20% too far",
            "  vertical      [range] Upward speed. A normal jump is 0.42, the vanilla spider leap 0.4",
            "  inaccuracy    [range] Blocks the landing spot is shifted in a random direction",
            "  maxSpeed      Caps the horizontal leap speed. A sprinting player moves at about 0.28",
            "  cooldown      [range] Ticks between leaps (20 ticks = 1 second)",
            "  chance        Chance to leap when all conditions are met. A failed roll retries 1 second later",
            "  lineOfSight   true = only leaps when it can see the target",
            "  noFall        true = no fall damage from its own leaps",
            "  removeVanilla true = removes the vanilla leap AI (spiders, wolves, ocelots)",
            "  priority      AI task priority, lower = more important. Vanilla spiders: 3 leap, 4 melee attack",
            "[range] settings take a fixed number (1.2) or a range (0.9~1.4) that is rolled again on every leap."
    })
    @Config.Name("Default Leap Settings")
    public String defaults = "minRange=4, maxRange=10, minHeight=-64, distance=1.0, vertical=0.5, inaccuracy=0, maxSpeed=2, cooldown=60, chance=0.3, lineOfSight=true, noFall=true, removeVanilla=false, priority=3";

    @Config.Comment({
            "Mobs that leap at their attack target. One mob per line: <target>, key=value, key=value, ...",
            "Any setting left out comes from \"Default Leap Settings\" (same keys).",
            "<target> is an entity id (minecraft:spider), or flag:<ForgeData key> for any mob with that ForgeData flag set to true.",
            "  flag:ScalingHealth.IsBlight       Scaling Health blights (the purple fire mobs)",
            "  flag:EagleMixinsOverworldAllowed  parasites spawned during WakingNights",
            "Entity ids win over flags. Flags are checked when the mob joins the world and for 1 second after, since some mods set them a bit later.",
            "Mobs can also get leaping through NBT: {ForgeData:{EagleMixinsLeap:1b}}, or a compound with the same keys to change single settings:",
            "  {ForgeData:{EagleMixinsLeap:{maxRange:12, distance:\"0.8~1.2\", cooldownMin:40, cooldownMax:80}}}",
            "EagleMixinsLeap:0b disables leaping for that mob even if a rule matches it.",
            "Changes only apply to mobs that (re)join the world, e.g. after a relog or chunk reload."
    })
    @Config.Name("Leap Rules")
    public String[] rules = {
            "minecraft:spider, minRange=3, maxRange=8, distance=0.85~1.15, vertical=0.4~0.55, inaccuracy=0~1, cooldown=40~80, chance=0.5, removeVanilla=true",
            "minecraft:cave_spider, minRange=3, maxRange=6, distance=0.85~1.1, vertical=0.4~0.55, inaccuracy=0~1, cooldown=40~80, chance=0.5, removeVanilla=true",
            "flag:ScalingHealth.IsBlight, maxRange=12, distance=0.85~1.2, vertical=0.5~0.65, inaccuracy=0~1.5, cooldown=60~100, chance=0.4"
    };

    private static final String FLAG_PREFIX = "flag:";

    private LeapSettings parsedDefaults = null;
    private Map<ResourceLocation, LeapSettings> byEntity = null;
    private Map<String, LeapSettings> byFlag = null;

    public void reset() {
        parsedDefaults = null;
        byEntity = null;
        byFlag = null;
    }

    public LeapSettings getDefaults() {
        if (parsedDefaults == null) parse();
        return parsedDefaults;
    }

    public Map<ResourceLocation, LeapSettings> getByEntity() {
        if (byEntity == null) parse();
        return byEntity;
    }

    public Map<String, LeapSettings> getByFlag() {
        if (byFlag == null) parse();
        return byFlag;
    }

    private void parse() {
        LeapSettings newDefaults = new LeapSettings().apply(defaults, defaults);
        Map<ResourceLocation, LeapSettings> newByEntity = new LinkedHashMap<>();
        Map<String, LeapSettings> newByFlag = new LinkedHashMap<>();

        for (String line : rules) {
            String[] parts = line.split(",", 2);
            if (parts[0].trim().isEmpty()) continue;
            LeapSettings settings = newDefaults.copy();
            if (parts.length > 1) settings.apply(parts[1], line);

            String target = parts[0].trim();
            if (target.startsWith(FLAG_PREFIX)) {
                String flag = target.substring(FLAG_PREFIX.length()).trim();
                if (flag.isEmpty()) EagleMixins.LOGGER.warn("Leaping Mobs: missing flag name in \"{}\"", line);
                else newByFlag.put(flag, settings);
            } else {
                newByEntity.put(new ResourceLocation(target), settings);
            }
        }

        parsedDefaults = newDefaults;
        byEntity = Collections.unmodifiableMap(newByEntity);
        byFlag = Collections.unmodifiableMap(newByFlag);
    }
}
