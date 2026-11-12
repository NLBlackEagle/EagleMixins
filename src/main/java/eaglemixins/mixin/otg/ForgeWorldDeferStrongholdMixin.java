package eaglemixins.mixin.otg;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.pg85.otg.common.LocalMaterialData;
import com.pg85.otg.configuration.biome.BiomeConfig;
import com.pg85.otg.forge.world.ForgeWorld;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.bo3.NamedBinaryTag;
import eaglemixins.util.IDeferredStronghold;
import eaglemixins.util.StructureStarts;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.MapGenStructure;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraft.world.gen.structure.StructureStart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
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

    // ---- Keeping OTG out of strongholds ----
    // Deferring only protects a stronghold from the population of its own chunk. OTG populates a 2x2 chunk area (32x32 blocks)
    // while a stronghold is placed per 16x16 area, so neighbours populated later wrote their ores, lakes, veins, BO3 objects
    // and BO4 parts straight through it. Now OTG skips every block inside a stronghold piece's bounding box: pieces that are
    // already placed stay intact, pieces that aren't placed yet would overwrite those blocks anyway.

    /** Stronghold piece boxes by chunk (ChunkPos.asLong), rebuilt when the strongholds change */
    @Unique private final Long2ObjectOpenHashMap<List<StructureBoundingBox>> eaglemixins$strongholdBoxes = new Long2ObjectOpenHashMap<>();
    @Unique private long eaglemixins$strongholdSignature = Long.MIN_VALUE;

    /** Once per chunk population: new strongholds get planned, BetterStronghold relocation moves pieces when one is first placed */
    @Inject(method = "placeDefaultStructures", at = @At("HEAD"))
    private void eaglemixins$refreshStrongholdBoxes(Random rand, ChunkCoordinate chunkCoord, CallbackInfoReturnable<Boolean> cir) {
        Long2ObjectMap<StructureStart> starts = StructureStarts.get(this.strongholdGen);
        if (starts == null) return;

        long signature = starts.size();
        for (StructureStart start : starts.values()) {
            if (!start.isSizeableStructure()) continue;
            StructureBoundingBox box = start.getBoundingBox();
            signature = signature * 31 + box.minX;
            signature = signature * 31 + box.minY;
            signature = signature * 31 + box.minZ;
            signature = signature * 31 + box.maxX;
            signature = signature * 31 + box.maxZ;
        }
        if (signature == this.eaglemixins$strongholdSignature) return;
        this.eaglemixins$strongholdSignature = signature;

        this.eaglemixins$strongholdBoxes.clear();
        for (StructureStart start : starts.values()) {
            if (!start.isSizeableStructure()) continue; // vanilla never places these
            for (StructureComponent component : start.getComponents()) {
                StructureBoundingBox box = component.getBoundingBox();
                for (int cx = box.minX >> 4; cx <= box.maxX >> 4; cx++)
                    for (int cz = box.minZ >> 4; cz <= box.maxZ >> 4; cz++)
                        this.eaglemixins$strongholdBoxes.computeIfAbsent(ChunkPos.asLong(cx, cz), k -> new ArrayList<>()).add(box);
            }
        }
    }

    /** Every block OTG places while populating (resources, BO3s, BO4s, ReplacedBlocks) goes through here */
    @Inject(
            method = "setBlock(IIILcom/pg85/otg/common/LocalMaterialData;Lcom/pg85/otg/util/bo3/NamedBinaryTag;Lcom/pg85/otg/util/ChunkCoordinate;Lcom/pg85/otg/configuration/biome/BiomeConfig;Z)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void eaglemixins$keepOutOfStrongholds(int x, int y, int z, LocalMaterialData material, NamedBinaryTag metaDataTag,
                                                  ChunkCoordinate chunkBeingPopulated, BiomeConfig biomeConfig, boolean replaceBlocks, CallbackInfo ci) {
        if (chunkBeingPopulated == null || this.eaglemixins$strongholdBoxes.isEmpty()) return; // only during population
        List<StructureBoundingBox> boxes = this.eaglemixins$strongholdBoxes.get(ChunkPos.asLong(x >> 4, z >> 4));
        if (boxes == null) return;
        for (StructureBoundingBox box : boxes) {
            if (x >= box.minX && x <= box.maxX && y >= box.minY && y <= box.maxY && z >= box.minZ && z <= box.maxZ) {
                ci.cancel();
                return;
            }
        }
    }
}
