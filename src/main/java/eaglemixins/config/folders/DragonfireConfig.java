package eaglemixins.config.folders;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Config;

import java.util.LinkedHashMap;
import java.util.Map;

public class DragonfireConfig {

    @Config.Comment({
            "Weapons that inflict Dragonfire on melee hits.",
            "Dragonfire sets the target on fire for 16 seconds per level and re-ignites it while the effect lasts.",
            "Level: effect level (1 = 16s, 2 = 32s, ...)",
            "Chance: chance per hit to apply the effect (0.0 - 1.0)"
    })
    @Config.Name("Dragonfire Weapons")
    public Map<ResourceLocation, DragonfireEntry> weapons = new LinkedHashMap<>();

    private static final String[] MELEE_TYPES = {
            "battleaxe", "dagger", "glaive", "greatsword", "halberd", "hammer", "katana", "lance", "longsword",
            "mace", "pike", "rapier", "saber", "scythe", "spear", "staff", "warhammer"
    };

    private void initWeapons() {
        for (String type : MELEE_TYPES) {
            weapons.put(new ResourceLocation("spartanfire", type + "_fire_dragonbone"), new DragonfireEntry(1, 1.0F));
            weapons.put(new ResourceLocation("spartanfire", type + "_fire_dragonsteel"), new DragonfireEntry(2, 1.0F));
        }
    }

    public static class DragonfireEntry {
        @Config.Name("Level")
        @Config.RangeInt(min = 1, max = 10)
        public int level;
        @Config.Name("Chance")
        @Config.RangeDouble(min = 0.0, max = 1.0)
        public float chance;

        public DragonfireEntry(){} //needed for betterconfig
        public DragonfireEntry(int level, float chance) {
            this.level = level;
            this.chance = chance;
        }
    }

    public DragonfireConfig() {
        initWeapons();
    }
}
