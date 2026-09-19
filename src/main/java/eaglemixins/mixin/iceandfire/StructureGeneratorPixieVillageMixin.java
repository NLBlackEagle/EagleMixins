package eaglemixins.mixin.iceandfire;

import com.github.alexthe666.iceandfire.event.StructureGenerator;
import com.github.alexthe666.iceandfire.world.village.MapGenPixieVillage;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import eaglemixins.compat.BiomeTagBlacklist;
import eaglemixins.config.ForgeConfigHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Random;

/**
 * MapGenPixieVillage#canSpawnStructureAtCoords is dead code - IceAndFire's StructureGenerator#generate
 * calls MapGenPixieVillage#generate directly after its own biome dictionary check
 * (FOREST && (SPOOKY || MAGICAL)), bypassing it entirely. Redirect that call instead so the biome tag
 * blacklist actually has an effect.
 */
@Mixin(StructureGenerator.class)
public class StructureGeneratorPixieVillageMixin {

    @WrapOperation(
            method = "generate",
            at = @At(value = "INVOKE", target = "Lcom/github/alexthe666/iceandfire/world/village/MapGenPixieVillage;generate(Lnet/minecraft/world/World;Ljava/util/Random;Lnet/minecraft/util/math/BlockPos;)Z")
    )
    private boolean eagleMixins_maybeGeneratePixieVillage(
            MapGenPixieVillage instance, World world, Random rand, BlockPos pos,
            Operation<Boolean> original,
            @Local(ordinal = 0, argsOnly = true) int chunkX,
            @Local(ordinal = 1, argsOnly = true) int chunkZ
    ) {
        if (BiomeTagBlacklist.isChunkBiomeBlacklisted(world, chunkX, chunkZ, ForgeConfigHandler.server.pixieVillageDisabledBiomeTags)) {
            return false;
        }
        return original.call(instance, world, rand, pos);
    }
}
