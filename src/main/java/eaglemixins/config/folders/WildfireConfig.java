package eaglemixins.config.folders;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Config;

import java.util.LinkedHashMap;
import java.util.Map;

public class WildfireConfig {

    @Config.Comment({
            "Weapons that inflict Wildfire on hit (melee, or thrown for Spartan Weaponry throwing weapons).",
            "Listed weapons get a tooltip from the lang key eaglemixins.tooltip.wildfire.<modid>, falling back to eaglemixins.tooltip.wildfire.",
            "Wildfire sets the target on fire for \"Seconds Per Level\" seconds per level.",
            "Level: effect level (fire duration = level * Seconds Per Level)",
            "Chance: chance per hit to apply the effect (0.0 - 1.0)"
    })
    @Config.Name("Wildfire Weapons")
    public Map<ResourceLocation, WildfireEntry> weapons = new LinkedHashMap<>();

    @Config.Comment("Seconds of fire (and effect duration) per Wildfire level. For reference: Fire Aspect is 4s per level, Flame is 5s, lava is 15s")
    @Config.Name("Seconds Per Level")
    @Config.RangeInt(min = 1, max = 600)
    public int secondsPerLevel = 16;

    @Config.Comment("If true, entities with Wildfire are re-ignited as soon as they leave water or rain, until the effect runs out. If false, it is a single ignition that water can put out")
    @Config.Name("Re-ignite While Active")
    public boolean reignite = true;

    @Config.Comment("Show a tooltip on listed weapons saying they inflict Wildfire")
    @Config.Name("Show Tooltip")
    public boolean showTooltip = true;

    private static final String[] SPARTANFIRE_TYPES = {
            "glaive", "katana", "scythe", "greatsword", "longsword", "saber", "rapier", "dagger", "spear", "pike",
            "lance", "halberd", "warhammer", "hammer", "battleaxe", "mace", "staff",
            "throwing_axe", "throwing_knife", "javelin", "boomerang"
    };

    private static final String[] BETTERSURVIVAL_TYPES = {
            "hammer", "spear", "dagger", "battleaxe", "nunchaku"
    };

    private void initWeapons() {
        for (String type : SPARTANFIRE_TYPES) {
            weapons.put(new ResourceLocation("spartanfire", type + "_fire_dragonbone"), new WildfireEntry(2, 1.0F));
        }
        for (String type : BETTERSURVIVAL_TYPES) {
            weapons.put(new ResourceLocation("mujmajnkraftsbettersurvival", "itemfiredragonbone" + type), new WildfireEntry(2, 1.0F));
        }
    }

    public static class WildfireEntry {
        @Config.Name("Level")
        @Config.RangeInt(min = 1, max = 10)
        public int level;
        @Config.Name("Chance")
        @Config.RangeDouble(min = 0.0, max = 1.0)
        public float chance;

        public WildfireEntry(){} //needed for betterconfig
        public WildfireEntry(int level, float chance) {
            this.level = level;
            this.chance = chance;
        }
    }

    public WildfireConfig() {
        initWeapons();
    }
}
