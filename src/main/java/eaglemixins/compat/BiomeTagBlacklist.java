package eaglemixins.compat;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;

import java.util.Set;

public class BiomeTagBlacklist {

    public static boolean isChunkBiomeBlacklisted(World world, int chunkX, int chunkZ, Set<String> tags) {
        if (tags == null || tags.isEmpty()) return false;

        Biome biome = world.getBiome(new BlockPos((chunkX << 4) + 8, 0, (chunkZ << 4) + 8));
        if (biome == null) return false;

        return tags.stream().anyMatch(tag -> BiomeDictionary.hasType(biome, BiomeDictionary.Type.getType(tag)));
    }
}
