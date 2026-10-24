package eaglemixins.config.folders;

import eaglemixins.EagleMixins;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Config;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LightningConfig {

    @Config.Comment({
            "Extra lightning in radioactive areas, wherever the radiation is high enough to show the dust particles",
            "(NuclearCraft Options > rad_particle_threshold), on top of vanilla lightning. Requires NuclearCraft"
    })
    @Config.Name("Radioactive Storms")
    public boolean radioactiveStorms = true;

    @Config.Comment({
            "Each tick, every loaded radioactive chunk near a player gets a 1 in this chance of a lightning strike while thundering.",
            "Vanilla is 1 in 100000, so 10000 is 10 times as much lightning. 0 = none"
    })
    @Config.Name("Radioactive Storms: Thunder Chance")
    @Config.RangeInt(min = 0)
    public int thunderChance = 10000;

    @Config.Comment({
            "Same as Thunder Chance, but while it only rains (vanilla has no lightning then).",
            "Also counts in biomes where it doesn't rain, like deserts. 0 = none"
    })
    @Config.Name("Radioactive Storms: Rain Chance")
    @Config.RangeInt(min = 0)
    public int rainChance = 40000;

    @Config.Comment("Same as Thunder Chance, but in clear weather. 0 = none")
    @Config.Name("Radioactive Storms: Clear Chance")
    @Config.RangeInt(min = 0)
    public int clearChance = 0;

    @Config.Comment("Red and black lightning bolts shooting diagonally through the Underneath, only visual (client side, no damage or fire)")
    @Config.Name("Underneath Lightning")
    public boolean underneathLightning = true;

    @Config.Comment("Dimension id of the Underneath")
    @Config.Name("Underneath Lightning: Dimension")
    public int underneathDimension = 3;

    @Config.Comment("Average seconds between bolts")
    @Config.Name("Underneath Lightning: Interval")
    @Config.RangeDouble(min = 0.5, max = 3600)
    public double underneathInterval = 20;

    @Config.Comment("Min and max blocks away from the player that bolts appear")
    @Config.Name("Underneath Lightning: Min Distance")
    @Config.RangeInt(min = 0, max = 256)
    public int underneathMinDistance = 0;

    @Config.Name("Underneath Lightning: Max Distance")
    @Config.RangeInt(min = 1, max = 256)
    public int underneathMaxDistance = 96;

    @Config.Comment("Hex colors of the bolts, each bolt picks one. Dark colors are drawn as dark bolts instead of glowing ones")
    @Config.Name("Underneath Lightning: Colors")
    public String[] underneathColors = {"#C00000", "#000000"};

    @Config.Comment("Volume of the thunder of a bolt, 0 = silent. The thunder is heard up to 16 x this many blocks away")
    @Config.Name("Underneath Lightning: Thunder Volume")
    @Config.RangeDouble(min = 0, max = 16)
    public float underneathVolume = 6;

    @Config.Comment("Purple and black lightning bolts shooting diagonally through the void of the End, only visual (client side, no damage or fire)")
    @Config.Name("End Lightning")
    public boolean endLightning = true;

    @Config.Comment("Dimension id of the End")
    @Config.Name("End Lightning: Dimension")
    public int endDimension = 1;

    @Config.Comment("Average seconds between bolts")
    @Config.Name("End Lightning: Interval")
    @Config.RangeDouble(min = 0.5, max = 3600)
    public double endInterval = 35;

    @Config.Comment("Min and max blocks away from the player that bolts appear")
    @Config.Name("End Lightning: Min Distance")
    @Config.RangeInt(min = 0, max = 256)
    public int endMinDistance = 48;

    @Config.Name("End Lightning: Max Distance")
    @Config.RangeInt(min = 1, max = 256)
    public int endMaxDistance = 128;

    @Config.Comment("Hex colors of the bolts, each bolt picks one. Dark colors are drawn as dark bolts instead of glowing ones")
    @Config.Name("End Lightning: Colors")
    public String[] endColors = {"#A020F0", "#CC00FA", "#000000"};

    @Config.Comment("Volume of the thunder of a bolt, 0 = silent. The thunder is heard up to 16 x this many blocks away")
    @Config.Name("End Lightning: Thunder Volume")
    @Config.RangeDouble(min = 0, max = 16)
    public float endVolume = 2.5F;

    @Config.Comment("Black and dark yellow lightning constantly striking down from the sky in the nuclear wasteland, only visual (client side, no damage or fire)")
    @Config.Name("Wasteland Lightning")
    public boolean wastelandLightning = true;

    @Config.Comment("Biomes the wasteland lightning strikes in")
    @Config.Name("Wasteland Lightning: Biomes")
    public String[] wastelandBiomes = {"nuclearcraft:nuclear_wasteland"};

    @Config.Comment("Average seconds between bolts while standing in one of the biomes, in any weather")
    @Config.Name("Wasteland Lightning: Interval")
    @Config.RangeDouble(min = 0.5, max = 3600)
    public double wastelandInterval = 20;

    @Config.Comment({
            "Real lightning striking in one of the biomes (storms, Radioactive Storms, tridents, commands) looks like wasteland lightning",
            "instead of the normal white bolt. Only changes the look, it still burns and damages like normal"
    })
    @Config.Name("Wasteland Lightning: Replace Normal Lightning")
    public boolean wastelandReplaceVanilla = true;

    @Config.Comment("Min and max blocks away from the player that bolts strike")
    @Config.Name("Wasteland Lightning: Min Distance")
    @Config.RangeInt(min = 0, max = 256)
    public int wastelandMinDistance = 0;

    @Config.Name("Wasteland Lightning: Max Distance")
    @Config.RangeInt(min = 1, max = 256)
    public int wastelandMaxDistance = 64;

    @Config.Comment("Hex colors of the bolts, each bolt picks one. Dark colors are drawn as dark bolts instead of glowing ones")
    @Config.Name("Wasteland Lightning: Colors")
    public String[] wastelandColors = {"#000000", "#3D3300"};

    @Config.Comment("Volume of the thunder of a bolt, 0 = silent. The thunder is heard up to 16 x this many blocks away")
    @Config.Name("Wasteland Lightning: Thunder Volume")
    @Config.RangeDouble(min = 0, max = 16)
    public float wastelandVolume = 8;

    @Config.Comment("Red and black lightning striking down from the sky in parasite biomes, only visual (client side, no damage or fire)")
    @Config.Name("Parasite Lightning")
    public boolean parasiteLightning = true;

    @Config.Comment("Biomes the parasite lightning strikes in")
    @Config.Name("Parasite Lightning: Biomes")
    public String[] parasiteBiomes = {"srparasites:biome_parasite"};

    @Config.Comment("Average seconds between bolts while standing in one of the biomes, in any weather")
    @Config.Name("Parasite Lightning: Interval")
    @Config.RangeDouble(min = 0.5, max = 3600)
    public double parasiteInterval = 20;

    @Config.Comment("Min and max blocks away from the player that bolts strike")
    @Config.Name("Parasite Lightning: Min Distance")
    @Config.RangeInt(min = 0, max = 256)
    public int parasiteMinDistance = 0;

    @Config.Name("Parasite Lightning: Max Distance")
    @Config.RangeInt(min = 1, max = 256)
    public int parasiteMaxDistance = 128;

    @Config.Comment("Hex colors of the bolts, each bolt picks one. Dark colors are drawn as dark bolts instead of glowing ones")
    @Config.Name("Parasite Lightning: Colors")
    public String[] parasiteColors = {"#C00000", "#000000"};

    @Config.Comment("Volume of the thunder of a bolt, 0 = silent. The thunder is heard up to 16 x this many blocks away")
    @Config.Name("Parasite Lightning: Thunder Volume")
    @Config.RangeDouble(min = 0, max = 16)
    public float parasiteVolume = 6;

    private int[] underneathColorValues = null;
    private int[] wastelandColorValues = null;
    private int[] parasiteColorValues = null;
    private int[] endColorValues = null;
    private Set<ResourceLocation> wastelandBiomeSet = null;
    private Set<ResourceLocation> parasiteBiomeSet = null;

    public int[] getUnderneathColors() {
        if (underneathColorValues == null) underneathColorValues = parseColors(underneathColors, 0xC00000);
        return underneathColorValues;
    }

    public int[] getWastelandColors() {
        if (wastelandColorValues == null) wastelandColorValues = parseColors(wastelandColors, 0x000000);
        return wastelandColorValues;
    }

    public int[] getParasiteColors() {
        if (parasiteColorValues == null) parasiteColorValues = parseColors(parasiteColors, 0xC00000);
        return parasiteColorValues;
    }

    public int[] getEndColors() {
        if (endColorValues == null) endColorValues = parseColors(endColors, 0xA020F0);
        return endColorValues;
    }

    public boolean isWastelandBiome(@Nullable ResourceLocation biome) {
        if (wastelandBiomeSet == null) wastelandBiomeSet = parseBiomes(wastelandBiomes);
        return biome != null && wastelandBiomeSet.contains(biome);
    }

    public boolean isParasiteBiome(@Nullable ResourceLocation biome) {
        if (parasiteBiomeSet == null) parasiteBiomeSet = parseBiomes(parasiteBiomes);
        return biome != null && parasiteBiomeSet.contains(biome);
    }

    private static Set<ResourceLocation> parseBiomes(String[] biomes) {
        Set<ResourceLocation> set = new HashSet<>();
        for (String id : biomes) set.add(new ResourceLocation(id.trim()));
        return set;
    }

    private static int[] parseColors(String[] colors, int fallback) {
        List<Integer> parsed = new ArrayList<>();
        for (String color : colors) {
            try {
                parsed.add(Integer.parseInt(color.trim().replace("#", ""), 16) & 0xFFFFFF);
            } catch (NumberFormatException e) {
                EagleMixins.LOGGER.error("Invalid lightning color {}", color);
            }
        }
        if (parsed.isEmpty()) parsed.add(fallback);
        return parsed.stream().mapToInt(Integer::intValue).toArray();
    }

    public void reset() {
        underneathColorValues = null;
        wastelandColorValues = null;
        parasiteColorValues = null;
        endColorValues = null;
        wastelandBiomeSet = null;
        parasiteBiomeSet = null;
    }
}
