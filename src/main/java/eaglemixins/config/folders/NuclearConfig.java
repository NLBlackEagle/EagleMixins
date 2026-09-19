package eaglemixins.config.folders;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Config;

import java.util.LinkedHashMap;
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
    public Map<ResourceLocation, Double> radiationResistanceList = new LinkedHashMap<>();

    @Config.Comment("Unopened containers with the given loot tables will radiate the given amount of radiation into their subchunk")
    @Config.Name("Radiating Loot Tables")
    public Map<String, Double> lootTableRadiation = new LinkedHashMap<>();

    @Config.Comment("Remove entries to have them not count for irradiating the player and the chunks around it. Or set to false to not check sub-inventories like contents of shulkerboxes, crates and toolbelts")
    @Config.Name("Radiating Inventories")
    public Map<String, Boolean> inventoryRadiation = new LinkedHashMap<>();

    public NuclearConfig(){
        radiationResistanceList.put(new ResourceLocation("iceandfire:firedragon"), 1000.);
        radiationResistanceList.put(new ResourceLocation("iceandfire:icedragon"), 1000.);
        radiationResistanceList.put(new ResourceLocation("iceandfire:lightningdragon"), 1000.);

        lootTableRadiation.put("dregora:ruins/nuclear", 0.1000); //value is average of such a loot table
        lootTableRadiation.put("dregora:ruins/starter", 0.0361); //value is average of such a loot table

        inventoryRadiation.put("enderChest", true);
        inventoryRadiation.put("inventoryCrafting", true);
        inventoryRadiation.put("mouseItem", true);
        inventoryRadiation.put("itemEntity", true);
        inventoryRadiation.put("backpack", true);
        inventoryRadiation.put("toolbeltSlot", true);
    }
}
