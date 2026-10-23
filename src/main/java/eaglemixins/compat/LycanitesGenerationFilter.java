package eaglemixins.compat;

import eaglemixins.config.ForgeConfigHandler;
import net.minecraft.world.World;

public class LycanitesGenerationFilter {

    public static boolean isChunkBiomeDisabled(World world, int chunkX, int chunkZ) {
        return BiomeTagBlacklist.isChunkBiomeBlacklisted(world, chunkX, chunkZ, ForgeConfigHandler.server.lycanitesGenerationDisabledBiomeTags);
    }

    public static boolean isDungeonChunkBiomeDisabled(World world, int chunkX, int chunkZ) {
        return BiomeTagBlacklist.isChunkBiomeBlacklisted(world, chunkX, chunkZ, ForgeConfigHandler.server.lycanitesDungeonDisabledBiomeTags);
    }
}
