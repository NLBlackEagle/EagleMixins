package eaglemixins.world;

import eaglemixins.config.ForgeConfigHandler;
import eaglemixins.config.folders.NetherThoriumConfig;
import eaglemixins.init.ModBlocks;
import net.minecraft.block.state.pattern.BlockMatcher;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraftforge.fml.common.IWorldGenerator;

import java.util.Random;

/** Generates thorium ore in netherrack, same way vanilla generates its overworld ores. */
public class NetherThoriumGenerator implements IWorldGenerator {

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        NetherThoriumConfig config = ForgeConfigHandler.netherthorium;
        if (!config.enabled || config.veinsPerChunk <= 0) return;
        if (!isDimensionEnabled(world.provider.getDimension(), config.dimensions)) return;
        if (random.nextDouble() >= config.chunkChance) return;

        int minY = Math.min(config.minHeight, config.maxHeight);
        int maxY = Math.max(config.minHeight, config.maxHeight);
        WorldGenMinable vein = new WorldGenMinable(ModBlocks.NETHER_THORIUM_ORE.getDefaultState(), config.veinSize, BlockMatcher.forBlock(Blocks.NETHERRACK));

        // No +8 offset here, WorldGenMinable already adds it itself
        int baseX = chunkX * 16;
        int baseZ = chunkZ * 16;
        for (int i = 0; i < config.veinsPerChunk; i++) {
            BlockPos pos = new BlockPos(baseX + random.nextInt(16), minY + random.nextInt(maxY - minY + 1), baseZ + random.nextInt(16));
            vein.generate(world, random, pos);
        }
    }

    private static boolean isDimensionEnabled(int dimension, int[] dimensions) {
        for (int dim : dimensions)
            if (dim == dimension) return true;
        return false;
    }
}
