package eaglemixins.config.folders;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Config;

import java.util.LinkedHashMap;
import java.util.Map;

public class LeapConfig {

    @Config.Comment("Master toggle. If false, no mob gets the leap AI from this config or from NBT.")
    @Config.Name("Enabled")
    public boolean enabled = true;

    @Config.Comment({
            "Mobs that leap at their attack target, by entity id.",
            "To add a mob, copy the \"minecraft:spider\" { ... } block below and rename it to the mob's id.",
            "Fields marked [range] take either a fixed number (\"1.2\") or a random range (\"0.9~1.4\") that is rolled again on every leap.",
            "Changes only apply to mobs that (re)join the world, e.g. after a relog or chunk reload."
    })
    @Config.Name("Leap Rules By Entity")
    public Map<ResourceLocation, Entry> byEntity = new LinkedHashMap<>();

    @Config.Comment({
            "Mobs that leap at their attack target, by ForgeData flag. Any mob with this flag set to true (1b) gets these settings.",
            "\"ScalingHealth.IsBlight\" matches Scaling Health blights, \"EagleMixinsOverworldAllowed\" the parasites spawned during WakingNights.",
            "Entity id rules take priority over these. Checked when the mob joins the world and for 1 second after, since some mods set their flag a bit later.",
            "Fields marked [range] take either a fixed number (\"1.2\") or a random range (\"0.9~1.4\")."
    })
    @Config.Name("Leap Rules By ForgeData Flag")
    public Map<String, Entry> byFlag = new LinkedHashMap<>();

    @Config.Comment({
            "Settings for mobs that only get leaping through NBT, e.g.",
            "/summon minecraft:zombie ~ ~ ~ {ForgeData:{EagleMixinsLeap:1b}}",
            "The NBT tag can also be a compound that overrides single settings on top of the mob's config rule (or these defaults):",
            "{ForgeData:{EagleMixinsLeap:{maxRange:12d, distance:\"0.8~1.2\", cooldownMin:40, cooldownMax:80}}}",
            "Keys: minRange, maxRange, minHeight, distance, vertical, inaccuracy, maxSpeed, cooldown, chance, lineOfSight, noFall, removeVanilla, priority.",
            "[range] keys take a number, a \"min~max\" string, or <key>Min + <key>Max.",
            "EagleMixinsLeap:0b disables leaping for that mob even if a config rule matches it."
    })
    @Config.Name("NBT Leap Defaults")
    public Entry nbtDefaults = new Entry();

    public LeapConfig() {
        byEntity.put(new ResourceLocation("minecraft:spider"), spiderDefaults(8.0, "0.85~1.15"));
        byEntity.put(new ResourceLocation("minecraft:cave_spider"), spiderDefaults(6.0, "0.85~1.1"));

        //Scaling Health blights (the purple fire mobs)
        Entry blight = new Entry();
        blight.minRange = 4.0;
        blight.maxRange = 12.0;
        blight.distanceMultiplier = "0.85~1.2";
        blight.verticalPower = "0.5~0.65";
        blight.inaccuracy = "0~1.5";
        blight.cooldown = "60~100";
        blight.chance = 0.4F;
        byFlag.put("ScalingHealth.IsBlight", blight);
    }

    private static Entry spiderDefaults(double maxRange, String distance) {
        Entry e = new Entry();
        e.minRange = 3.0;
        e.maxRange = maxRange;
        e.distanceMultiplier = distance;
        e.verticalPower = "0.4~0.55";
        e.inaccuracy = "0~1";
        e.cooldown = "40~80";
        e.chance = 0.5F;
        e.removeVanillaLeap = true;
        return e;
    }

    public static class Entry {
        @Config.Comment("Minimum horizontal distance to the target (blocks) before the mob will leap.")
        @Config.Name("Min Range")
        @Config.RangeDouble(min = 0)
        public double minRange = 4.0;

        @Config.Comment("Maximum horizontal distance to the target (blocks) at which the mob will leap.")
        @Config.Name("Max Range")
        @Config.RangeDouble(min = 0)
        public double maxRange = 10.0;

        @Config.Comment({
                "Only leap if the target is at least this many blocks higher than the mob.",
                "E.g. 2 = only leap at players who pillared up. -64 = leap regardless of height."
        })
        @Config.Name("Min Target Height Above Mob")
        public double minHeightAbove = -64.0;

        @Config.Comment({
                "[range] How far the leap goes compared to the target's distance. 1 = lands right on the target,",
                "0.8 = falls 20% short, 1.2 = overshoots by 20%. A range like \"0.8~1.2\" makes them sometimes miss."
        })
        @Config.Name("Distance Multiplier")
        public String distanceMultiplier = "1.0";

        @Config.Comment("[range] Upward speed of the leap. A normal mob jump is 0.42, vanilla spider leap is 0.4.")
        @Config.Name("Vertical Power")
        public String verticalPower = "0.5";

        @Config.Comment("[range] Blocks the landing spot is shifted in a random direction. 0 = aims straight at the target.")
        @Config.Name("Inaccuracy")
        public String inaccuracy = "0";

        @Config.Comment("Caps the horizontal speed of the leap so long distance leaps don't turn into rockets. A sprinting player moves at about 0.28.")
        @Config.Name("Max Horizontal Speed")
        @Config.RangeDouble(min = 0)
        public double maxHorizontalSpeed = 2.0;

        @Config.Comment("[range] Ticks to wait after a leap before the next one (20 ticks = 1 second).")
        @Config.Name("Cooldown Ticks")
        public String cooldown = "60";

        @Config.Comment("Chance to leap when all conditions are met. On a failed roll the mob tries again 1 second later.")
        @Config.Name("Chance")
        @Config.RangeDouble(min = 0, max = 1)
        public float chance = 0.3F;

        @Config.Comment("If true, the mob only leaps when it can see its target.")
        @Config.Name("Requires Line Of Sight")
        public boolean lineOfSight = true;

        @Config.Comment("If true, the mob takes no fall damage from its own leaps.")
        @Config.Name("Prevent Fall Damage")
        public boolean preventFallDamage = true;

        @Config.Comment("If true, removes the mob's vanilla leap AI (spiders, ocelots, wolves) so only this leap is used.")
        @Config.Name("Remove Vanilla Leap AI")
        public boolean removeVanillaLeap = false;

        @Config.Comment({
                "Where the leap goes in the mob's AI task list. Lower = more important.",
                "It interrupts movement and attack tasks with a higher number. Vanilla spiders: 3 leap, 4 melee attack."
        })
        @Config.Name("AI Task Priority")
        @Config.RangeInt(min = 0)
        public int aiPriority = 3;

        public Entry() {} //needed for betterconfig
    }
}
