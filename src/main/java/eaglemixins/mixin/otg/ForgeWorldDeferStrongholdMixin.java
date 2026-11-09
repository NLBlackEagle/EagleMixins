package eaglemixins.mixin.otg;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.pg85.otg.forge.world.ForgeWorld;
import eaglemixins.util.IDeferredStronghold;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.MapGenStructure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Random;

/**
 * OTG places the stronghold before running the biome resource sequence (ores, veins, lakes, liquids, BO3s, ReplacedBlocks),
 * which then carve into it. Here the stronghold placement is only remembered, see ObjectSpawnerDeferStrongholdMixin.
 */
@Mixin(value = ForgeWorld.class, remap = false)
public abstract class ForgeWorldDeferStrongholdMixin implements IDeferredStronghold {
    @Shadow private MapGenStructure strongholdGen;
    @Shadow public World world;

    @Unique private ChunkPos eaglemixins$deferredStronghold;

    @WrapOperation(
            method = "placeDefaultStructures",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/gen/structure/MapGenStructure;generateStructure(Lnet/minecraft/world/World;Ljava/util/Random;Lnet/minecraft/util/math/ChunkPos;)Z", remap = true)
    )
    private boolean eaglemixins$deferStronghold(MapGenStructure gen, World world, Random rand, ChunkPos pos, Operation<Boolean> original) {
        if (gen != null && gen == this.strongholdGen) {
            this.eaglemixins$deferredStronghold = pos;
            return false;
        }
        return original.call(gen, world, rand, pos);
    }

    @Override
    public void eaglemixins$placeDeferredStronghold(Random rand) {
        ChunkPos pos = this.eaglemixins$deferredStronghold;
        if (pos == null) return;
        this.eaglemixins$deferredStronghold = null;
        this.strongholdGen.generateStructure(this.world, rand, pos);
    }
}
