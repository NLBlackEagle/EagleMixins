package eaglemixins.config.folders;

import net.minecraftforge.common.config.Config;

import java.util.Arrays;
import java.util.List;

public class SRParasiteConfig {

    @Config.Comment("Treat the allowed biomes list as whitelist. Set to false to treat as blacklist")
    @Config.Name("SRParasites allowed biomes is whitelist")
    public boolean biomeListIsWhitelist = true;

    //Parasites will be allowed to spawn via spawners, stay alive and will drop (reduced) loot in these biomes
    @Config.Comment("List of biome IDs to whitelist or blacklist depending on biomeListIsWhitelist.\n" +
            "This list supports the * wildcard, example: biomesoplenty:* would whitelist all biomesoplenty biomes.")
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

    @Config.Comment("Parasite display names that are allowed to always drop loot (even outside allowed biomes).\n" +
            "This matches the entity's *custom display name*, not its ID.\n" +
            "It's enough if the custom name contains any of these listed strings for it to be always allowed to drop loot.")
    @Config.Name("Parasite full loot-drop enabler")
    public String[] keepLootNames = {
            "Sentient Horror",
            "Degrading Overseer",
            "Malformed Observer",
            "Shivaxi",
            "Corrupted Carrier",
            "Necrotic Blight"
    };

    @Config.Comment("All Beckons near one beckon will be killed")
    @Config.Name("Kill Beckon nearby")
    public boolean killNearbyBeckon = true;

    @Config.Comment("Range in radius blocks searched around the beckon for nearby beckons")
    @Config.Name("Kill Beckon nearby range")
    public int killNearbyBeckonRange = 32;

    @Config.Comment("All SRParasites outside of the allowed biomes in the allowed biome whitelist will automatically be killed")
    @Config.Name("Kill Parasites outside alllowed biomes")
    public boolean killEscapedParasites = true;

    @Config.Comment("Parasite drops in the overworld have a chance to instead drop as corrupted ashes. This is the chance for that to happen.")
    @Config.Name("Corrupted Ashes chance")
    public float chanceCorruptedAshes = 0.375f;

    List<String> allowedBiomeList = null;
    public List<String> getAllowedBiomeList(){
        if(allowedBiomeList == null)
            allowedBiomeList = Arrays.asList(biomeList);
        return allowedBiomeList;
    }

    List<String> allowedParasiteNamesLoot = null;
    public List<String> getAllowedParasiteNamesLoot(){
        if(allowedParasiteNamesLoot == null)
            allowedParasiteNamesLoot = Arrays.asList(keepLootNames);
        return allowedParasiteNamesLoot;
    }

    public void reset(){
        allowedBiomeList = null;
        allowedParasiteNamesLoot = null;
    }
}
