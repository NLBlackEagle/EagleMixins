package eaglemixins.config.folders;

import net.minecraftforge.common.config.Config;

import java.util.HashMap;
import java.util.Map;

public class NuclearConfig {
    @Config.Comment("Threshold at which point radiation becomes visible through particles configured under client config section.")
    @Config.Name("Radiation Particle Threshold")
    public double rad_particle_threshold = 0.001;

    @Config.Comment("The radiation in the current subchunk needs to be at least a factor of (this + 1) times higher than the subchunk above/below to spread to that subchunk.")
    @Config.Name("Vertical Radiation Spread Gradient")
    public float radiation_spread_gradient_vertical = 0.4F;

    @Config.Comment({
            "Radiation resistance per entity.",
            "Format: <entity_id>=<value>",
            "Example: minecraft:sheep=1.0"
    })
    @Config.Name("RadiationResistanceList")
    public String[] radiationResistanceList = new String[] {
            "iceandfire:firedragon=1000.0",
            "iceandfire:icedragon=1000.0",
            "iceandfire:lightningdragon=1000.0"
    };

    @Config.Comment("Unopened containers with the given loot tables will radiate the given amount of radiation into their subchunk")
    @Config.Name("Radiating Loot Tables")
    public Map<String, Double> lootTableRadiation = new HashMap<String, Double>(){{
        put("dregora:ruins/nuclear", 0.1000); //value is average of such a loot table
        put("dregora:ruins/starter", 0.0361); //value is average of such a loot table
    }};

    @Config.Comment("Remove entries to have them not count for irradiating the player and the chunks around it. Or set to false to not check sub-inventories like shulker box or crate contents")
    @Config.Name("Radiating Inventories")
    public Map<String, Boolean> inventoryRadiation = new HashMap<String, Boolean>(){{
        put("enderChest", true);
        put("inventoryCrafting", true);
        put("mouseItem", true);
        put("itemEntity", true);
        put("backpack", true);
    }};
}
