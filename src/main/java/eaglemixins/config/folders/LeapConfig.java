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
            "  leapDistance  [range] Fixed leap length in blocks towards the target (e.g. 6~10), replaces \"distance\". 0 = use \"distance\"",
            "  maxLeap       Longest leap in blocks. Targets further away (up to maxRange) still get leapt at, the mob lands this far towards them. 0 = no limit",
            "  inaccuracy    [range] Blocks the landing spot is shifted in a random direction",
            "  arc           true = flies a smooth, symmetric arch to the landing spot. false = a single physics push (flat start, steep drop)",
            "  arcHeight     [range] (arc=true) Height of the arch compared to the leap length. 0.25 = a 8 block leap peaks 2 blocks high (min 0.65 for a 2 block hop up to 1.25 from 6 blocks)",
            "  arcSpeed      [range] (arc=true) Horizontal speed in blocks per tick. A sprinting player moves at about 0.28",
            "  vertical      [range] (arc=false) Upward speed. A normal jump is 0.42, the vanilla spider leap 0.4",
            "  maxSpeed      (arc=false) Caps the horizontal leap speed",
            "  cooldown      [range] Ticks between leaps (20 ticks = 1 second)",
            "  chance        Chance to leap when all conditions are met. A failed roll retries 1 second later",
            "  lineOfSight   true = only leaps when it can see the target",
            "  noFall        true = no fall damage from its own leaps",
            "  removeVanilla true = removes the vanilla leap AI (spiders, wolves, ocelots)",
            "  homerun       (arc=true) true = hitting a mob out of the air bats it away from the attacker on an arch of its own, with an anvil clang:",
            "                it climbs steeply, levels out at the far end, then drops. Stronger knockback flies further and faster,",
            "                above sprint + Knockback II it also leaves a particle trail. false = hits don't stop the leap (knockback does nothing mid-leap)",
            "  homerunPower  How far the homerun flies compared to the same knockback from the ground. 2 = twice as far (a plain hit ~5 blocks, Knockback II ~18)",
            "  homerunMax    Longest homerun in blocks (Advanced Knockback II reaches it). Knockback resistance shortens the homerun",
            "  priority      AI task priority, lower = more important. Vanilla spiders: 3 leap, 4 melee attack",
            "                Always moved in front of the mob's melee attack AI (zombies attack at 2, so they leap at 1), or it could never leap while chasing",
            "[range] settings take a fixed number (1.2) or a range (0.9~1.4) that is rolled again on every leap."
    })
    @Config.Name("Default Leap Settings")
    public String defaults = "minRange=3, maxRange=10, minHeight=-64, distance=1.0, leapDistance=0, maxLeap=0, arc=true, arcHeight=0.25~0.3, arcSpeed=0.5~0.6, vertical=0.5, inaccuracy=0, maxSpeed=2, cooldown=40~80, chance=0.3, lineOfSight=true, noFall=true, removeVanilla=false, homerun=true, homerunPower=2.0, homerunMax=30, priority=3";

    @Config.Comment({
            "Mobs that leap at their attack target. One mob per line: <target>, key=value, key=value, ...",
            "Any setting left out comes from \"Default Leap Settings\" (same keys).",
            "<target> is an entity id (minecraft:spider), or flag:<ForgeData key> for any mob with that ForgeData flag set to true.",
            "  flag:ScalingHealth.IsBlight       Scaling Health blights (the purple fire mobs)",
            "  flag:EagleMixinsOverworldAllowed  parasites spawned during WakingNights",
            "Flags win over entity ids (a blight zombie leaps like a blight). Flags are checked when the mob joins the world and for 1 second after, since some mods set them a bit later.",
            "Mobs can also get leaping through NBT: {ForgeData:{EagleMixinsLeap:1b}}, or a compound with the same keys to change single settings:",
            "  {ForgeData:{EagleMixinsLeap:{maxRange:12, distance:\"0.8~1.2\", cooldownMin:40, cooldownMax:80}}}",
            "EagleMixinsLeap:0b disables leaping for that mob even if a rule matches it.",
            "Changes only apply to mobs that (re)join the world, e.g. after a relog or chunk reload."
    })
    @Config.Name("Leap Rules")
    public String[] rules = {
            "minecraft:spider, minRange=3, maxRange=8, distance=0.85~1.15, arcHeight=0.2~0.3, arcSpeed=0.5~0.65, inaccuracy=0~1, cooldown=40~80, chance=0.25, removeVanilla=true",
            "minecraft:cave_spider, minRange=2, maxRange=6, distance=0.85~1.1, arcHeight=0.2~0.25, arcSpeed=0.55~0.7, inaccuracy=0~1, cooldown=30~60, chance=0.25, removeVanilla=true",
            "iceandfire:dread_scuttler, minRange=3, maxRange=9, distance=0.85~1.15, arcHeight=0.22~0.3, arcSpeed=0.5~0.6, inaccuracy=0~1, cooldown=50~90, chance=0.25",
            "defiledlands:scuttler, minRange=3, maxRange=8, distance=0.85~1.15, arcHeight=0.2~0.3, arcSpeed=0.5~0.65, inaccuracy=0~1, cooldown=40~80, chance=0.25",
            "lycanitesmobs:tarantula, minRange=3, maxRange=10, distance=0.85~1.15, arcHeight=0.25~0.3, arcSpeed=0.5~0.6, inaccuracy=0~1, cooldown=50~90, chance=0.25",
            "lycanitesmobs:frostweaver, minRange=3, maxRange=8, distance=0.85~1.15, arcHeight=0.2~0.3, arcSpeed=0.5~0.65, inaccuracy=0~1, cooldown=40~80, chance=0.25",
            "lycanitesmobs:trite, minRange=2, maxRange=6, distance=0.85~1.1, arcHeight=0.2~0.25, arcSpeed=0.55~0.7, inaccuracy=0~1, cooldown=30~60, chance=0.25",
            "minecraft:wolf, minRange=3, maxRange=8, distance=0.9~1.1, arcHeight=0.15~0.22, arcSpeed=0.6~0.75, cooldown=40~80, chance=0.3, removeVanilla=true",
            "minecraft:ocelot, minRange=2, maxRange=7, distance=0.9~1.1, arcHeight=0.25~0.3, arcSpeed=0.6~0.7, cooldown=40~80, chance=0.3, removeVanilla=true",
            "quark:foxhound, minRange=3, maxRange=8, distance=0.9~1.1, arcHeight=0.15~0.22, arcSpeed=0.6~0.75, cooldown=40~80, chance=0.3",
            "iceandfire:dread_beast, minRange=3, maxRange=9, distance=0.9~1.1, arcHeight=0.15~0.22, arcSpeed=0.6~0.75, cooldown=40~80, chance=0.3",
            "lycanitesmobs:uvaraptor, minRange=4, maxRange=12, distance=0.9~1.1, arcHeight=0.18~0.25, arcSpeed=0.65~0.8, cooldown=50~90, chance=0.3",
            "lycanitesmobs:ventoraptor, minRange=4, maxRange=12, distance=0.9~1.1, arcHeight=0.18~0.25, arcSpeed=0.6~0.75, cooldown=50~90, chance=0.3",
            "lycanitesmobs:warg, minRange=4, maxRange=10, distance=0.9~1.1, arcHeight=0.22~0.3, arcSpeed=0.55~0.65, cooldown=60~100, chance=0.3",
            "lycanitesmobs:maug, minRange=4, maxRange=10, distance=0.9~1.1, arcHeight=0.22~0.3, arcSpeed=0.55~0.65, cooldown=60~100, chance=0.3",
            "lycanitesmobs:barghest, minRange=4, maxRange=10, distance=0.9~1.1, arcHeight=0.22~0.3, arcSpeed=0.55~0.65, cooldown=60~100, chance=0.3",
            "lycanitesmobs:chupacabra, minRange=3, maxRange=8, distance=0.9~1.1, arcHeight=0.15~0.22, arcSpeed=0.6~0.75, cooldown=40~80, chance=0.3",
            "lycanitesmobs:lurker, minRange=3, maxRange=10, distance=0.95~1.1, arcHeight=0.18~0.25, arcSpeed=0.6~0.75, cooldown=60~100, chance=0.4",
            "lycanitesmobs:dawon, minRange=4, maxRange=10, distance=0.9~1.1, arcHeight=0.25~0.3, arcSpeed=0.55~0.65, cooldown=60~100, chance=0.3",
            "lycanitesmobs:bobeko, minRange=2, maxRange=7, distance=0.9~1.1, arcHeight=0.25~0.3, arcSpeed=0.6~0.7, cooldown=40~80, chance=0.3",
            "lycanitesmobs:feradon, minRange=4, maxRange=10, distance=0.9~1.1, arcHeight=0.22~0.3, arcSpeed=0.55~0.65, cooldown=60~100, chance=0.3",
            "lycanitesmobs:geken, minRange=2, maxRange=6, distance=0.9~1.1, arcHeight=0.25~0.35, arcSpeed=0.4~0.5, cooldown=30~60, chance=0.3",
            "lycanitesmobs:salamander, minRange=3, maxRange=8, distance=0.9~1.1, arcHeight=0.2~0.25, arcSpeed=0.5~0.6, cooldown=60~100, chance=0.3",
            "lycanitesmobs:darkling, minRange=2, maxRange=6, distance=0.9~1.1, arcHeight=0.25~0.35, arcSpeed=0.4~0.5, cooldown=30~60, chance=0.3",
            "lycanitesmobs:gorgomite, minRange=2, maxRange=6, distance=0.9~1.1, arcHeight=0.25~0.35, arcSpeed=0.4~0.5, cooldown=30~60, chance=0.3",
            "minecraft:zombie, minRange=3, maxRange=10, maxLeap=8, distance=0.9~1.05, arcHeight=0.2~0.3, arcSpeed=0.45~0.6, inaccuracy=0~1.5, cooldown=60~140, chance=0.25",
            "minecraft:husk, minRange=3, maxRange=10, maxLeap=8, distance=0.9~1.05, arcHeight=0.2~0.3, arcSpeed=0.45~0.6, inaccuracy=0~1.5, cooldown=60~140, chance=0.25",
            "minecraft:zombie_villager, minRange=3, maxRange=10, maxLeap=8, distance=0.9~1.05, arcHeight=0.2~0.3, arcSpeed=0.45~0.6, inaccuracy=0~1.5, cooldown=60~140, chance=0.25",
            "flag:ScalingHealth.IsBlight, minRange=3, maxRange=12, maxLeap=10, distance=0.85~1.2, arcHeight=0.25~0.3, arcSpeed=0.55~0.7, inaccuracy=0~1.5, cooldown=60~100, chance=0.4"
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
