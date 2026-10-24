package eaglemixins.config.folders;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Config;

import java.util.LinkedHashMap;
import java.util.Map;

public class AngryMobConfig {
    @Config.Comment({
            "Gives untamed entities (EntityCreature) a chance to turn hostile toward a player they see.",
            "When an eligible mob sees a player it rolls 'chance' once for that sighting; on a hit it becomes",
            "an angry mob and hunts that player until the player leaves its line of sight, then calms down.",
            "A miss is rolled again the next time a player comes into view.",
            "Note that this only sets the player as an attack target. The angry mob needs to know what to do when targeting a player.",
            "To add a mob, copy the \"minecraft:wolf\" { ... } block below and rename it to the mob's id, e.g. \"minecraft:polar_bear\"."
    })
    @Config.Name("Angry Mob Rules")
    public Map<ResourceLocation, Entry> angryMobs = new LinkedHashMap<>();
    private void initAngryMobsMap() {
        angryMobs.put(new ResourceLocation("minecraft:wolf"), new Entry(false, true, 0.1F));
    }
    public static class Entry {
        @Config.Name("Affects baby entities")
        public boolean baby;
        @Config.Name("Affects adult entities")
        public boolean adult;
        @Config.Name("Chance to trigger per sighting")
        @Config.RangeDouble(min = 0, max = 1)
        public float chance;
        @Config.Comment({
                "Where the angry targeting goes in the mob's target task list. Lower = more important.",
                "It can only interrupt targeting with a higher number, equal numbers don't interrupt each other.",
                "Vanilla wolf example: 1-2 defend/help owner, 3 fight back when hurt, 4 hunt sheep/rabbits, 5 attack skeletons.",
                "So at 4 getting hit still overrides the anger, and the wolf finishes hunting a sheep first. Use 3 or lower to override fighting back"
        })
        @Config.Name("AI Task Priority")
        @Config.RangeInt(min = 0)
        public int aiPriority = 4;

        public Entry(){} //needed for betterconfig
        public Entry(boolean baby, boolean adult, float chance) {
            this.baby = baby;
            this.adult = adult;
            this.chance = chance;
        }
    }

    @Config.Comment("Ticks an angry mob keeps hunting after losing line of sight of its target before it calms down (20 ticks = 1 second).")
    @Config.Name("Lose Sight Grace Ticks")
    @Config.RangeInt(min = 0)
    public int loseSightTicks = 60;

    public AngryMobConfig(){
        initAngryMobsMap();
    }
}