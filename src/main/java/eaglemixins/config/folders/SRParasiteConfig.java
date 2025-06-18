package eaglemixins.config.folders;

import net.minecraftforge.common.config.Config;

public class SRParasiteConfig {

    public boolean biomeListIsWhitelist = true;

    //Parasites will be allowed to spawn via spawners, stay alive and will drop (reduced) loot in these biomes
    @Config.Comment({
            "List of biome IDs to whitelist or blacklist depending on biomeListIsWhitelist.",
            "This list supports the * wildcard, example: biomesoplenty:* would whitelist all biomesoplenty biomes."

    })
    @Config.Name("SRParasites allowed biomes")
    public String[] biomeList = {
            "biomesoplenty:heath",
            "biomesoplenty:steppe",
            "biomesoplenty:wasteland",
            "openterraingenerator:overworld_abyssal_rift",
            "srparasites:biome_parasite",
            "openterraingenerator:overworld_lair_of_the_thing",
            "openterraingenerator:overworld_nuclear_ruins",
            "openterraingenerator:overworld_ruins_of_blight"
    };

    @Config.Comment({
            "Parasite display names that are allowed to always drop loot (even outside allowed biomes).",
            "This matches the entity's *custom display name*, not its ID."
    })
    @Config.Name("Parasite full loot-drop enabler")
    public String[] keepLootNames = {
            "Sentient Horror",
            "Degrading Overseer",
            "Malformed Observer",
            "Shivaxi",
            "Corrupted Carrier",
            "Necrotic Blight"
    };

    public void reset(){
        biomeList = null;
        keepLootNames = null;
    }
}
