package eaglemixins.config.folders;

import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Config;

import java.util.HashSet;
import java.util.Set;

public class SafeSpawnConfig {

    @Config.Comment({
            "If true, random spawns (first join, respawning without a bed) prefer spots inside buildings: a 2 block high space",
            "under a roof that is not natural terrain (see Natural Blocks), anywhere within Search Radius of the random spawn point.",
            "If false, or no such spot is found, players spawn on the highest safe spot (ground or a rooftop)."
    })
    @Config.Name("Prefer Structure Spawns")
    public boolean preferStructures = true;

    @Config.Comment({
            "Blocks around the random spawn point that are searched for a safe spot (and a building to spawn in).",
            "All chunks this touches (plus one around) get fully generated first, so bigger values make spawning take longer."
    })
    @Config.Name("Search Radius")
    @Config.RangeInt(min = 0, max = 48)
    public int searchRadius = 16;

    @Config.Comment("Max blocks between the floor a player spawns on and the roof above it to count as inside a building")
    @Config.Name("Max Roof Height")
    @Config.RangeInt(min = 2, max = 64)
    public int maxRoofHeight = 12;

    @Config.Comment({
            "Blocks that never count as a building roof, as <modid:block> or <modid> for a whole mod.",
            "Dirt, grass, sand, gravel, clay, snow, ice, logs and leaves never count already.",
            "Add natural stone types here if players end up spawning in caves or under overhangs."
    })
    @Config.Name("Natural Blocks")
    public String[] naturalBlocks = {
            "minecraft:stone",
            "minecraft:sandstone",
            "minecraft:red_sandstone",
            "minecraft:hardened_clay",
            "minecraft:stained_hardened_clay",
            "minecraft:netherrack",
            "minecraft:end_stone",
            "minecraft:bedrock",
            "minecraft:monster_egg",
            "minecraft:coal_ore",
            "minecraft:iron_ore",
            "minecraft:gold_ore",
            "minecraft:diamond_ore",
            "minecraft:emerald_ore",
            "minecraft:lapis_ore",
            "minecraft:redstone_ore",
            "minecraft:lit_redstone_ore",
            "dynamictrees"
    };

    private Set<String> naturalSet = null;

    public boolean isNatural(Block block) {
        if (naturalSet == null) {
            naturalSet = new HashSet<>();
            for (String entry : naturalBlocks) if (!entry.trim().isEmpty()) naturalSet.add(entry.trim());
        }
        ResourceLocation name = block.getRegistryName();
        return name != null && (naturalSet.contains(name.toString()) || naturalSet.contains(name.getNamespace()));
    }

    public void reset() {
        naturalSet = null;
    }
}
