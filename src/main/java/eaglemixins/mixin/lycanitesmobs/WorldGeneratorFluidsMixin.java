package eaglemixins.mixin.lycanitesmobs;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.lycanitesmobs.core.worldgen.WorldGeneratorFluids;
import eaglemixins.compat.LycanitesGenerationFilter;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Random;

@Mixin(WorldGeneratorFluids.class)
public class WorldGeneratorFluidsMixin {
    @WrapMethod(method = "generate", remap = false)
    private void eagleMixins$skipDisabledBiomes(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider, Operation<Void> original) {
        if (LycanitesGenerationFilter.isChunkBiomeDisabled(world, chunkX, chunkZ))
            return;
        original.call(random, chunkX, chunkZ, world, chunkGenerator, chunkProvider);
    }
}
