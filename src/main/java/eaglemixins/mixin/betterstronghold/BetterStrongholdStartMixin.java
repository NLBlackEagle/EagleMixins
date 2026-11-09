package eaglemixins.mixin.betterstronghold;

import com.unseen.bt.common.world.stronghold.MapGenBetterStronghold;
import eaglemixins.EagleMixins;
import eaglemixins.config.ForgeConfigHandler;
import eaglemixins.config.folders.BetterStrongholdConfig;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraft.world.gen.structure.StructureStart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

/**
 * Moves a new BetterStronghold to solid ground. The layout is decided while chunks are still being generated, so the terrain
 * can't be checked there. Instead the check runs right before the first piece of the stronghold gets placed (nothing is placed yet),
 * MapGenStructure saves the start again right after, so the new position is kept.
 */
@Mixin(MapGenBetterStronghold.Start.class)
public abstract class BetterStrongholdStartMixin extends StructureStart {

    @Unique private static final String NBT_KEY = "EagleMixinsSiteCheck";
    @Unique private static final int[][] DIRECTIONS = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {1, -1}, {-1, 1}, {-1, -1}};

    /** Only strongholds created with this mixin active get checked, ones loaded from older saves might be partially placed already */
    @Unique private boolean eaglemixins$needsSiteCheck = false;

    @Inject(method = "<init>(Lnet/minecraft/world/World;Ljava/util/Random;II)V", at = @At("TAIL"), remap = false)
    private void eaglemixins$markNew(World world, Random rand, int chunkX, int chunkZ, CallbackInfo ci) {
        this.eaglemixins$needsSiteCheck = true;
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setBoolean(NBT_KEY, this.eaglemixins$needsSiteCheck);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        this.eaglemixins$needsSiteCheck = tag.getBoolean(NBT_KEY);
    }

    @Inject(method = {"generateStructure", "func_75068_a"}, at = @At("HEAD"), remap = false)
    private void eaglemixins$checkSite(World world, Random rand, StructureBoundingBox sbb, CallbackInfo ci) {
        if (!this.eaglemixins$needsSiteCheck) return;
        this.eaglemixins$needsSiteCheck = false;

        BetterStrongholdConfig cfg = ForgeConfigHandler.betterstronghold;
        if (cfg.relocationDistance <= 0 || this.components.isEmpty() || !(world instanceof WorldServer)) return;

        StructureBoundingBox room = this.components.get(0).getBoundingBox();
        int centerX = (room.minX + room.maxX) >> 1;
        int centerZ = (room.minZ + room.maxZ) >> 1;
        ChunkProviderServer provider = ((WorldServer) world).getChunkProvider();

        // null = unknown (chunk exists on disk but isn't loaded, loading it here would populate it): keep the stronghold where it is
        Boolean solid = eaglemixins$isSolid(provider, centerX >> 4, centerZ >> 4, room.minY, room.maxY, cfg.minSolidFraction);
        if (solid == null || solid) return;

        int step = cfg.relocationDistance * 16;
        for (int[] dir : DIRECTIONS) {
            int dx = dir[0] * step, dz = dir[1] * step;
            Boolean candidate = eaglemixins$isSolid(provider, (centerX + dx) >> 4, (centerZ + dz) >> 4, room.minY, room.maxY, cfg.minSolidFraction);
            if (candidate != null && candidate) {
                for (StructureComponent component : this.components) component.offset(dx, 0, dz);
                this.updateBoundingBox();
                EagleMixins.LOGGER.debug("Better Stronghold: moved stronghold at chunk {}, {} by {}, {} blocks to solid ground", this.getChunkPosX(), this.getChunkPosZ(), dx, dz);
                return;
            }
        }
        EagleMixins.LOGGER.debug("Better Stronghold: no solid spot found for stronghold at chunk {}, {}, keeping it in place", this.getChunkPosX(), this.getChunkPosZ());
    }

    /**
     * Samples every second block of the chunk between minY and maxY.
     * Returns null if the chunk can't be checked without side effects, or if it's populated already (moving there would miss pieces).
     */
    @Unique
    private static Boolean eaglemixins$isSolid(ChunkProviderServer provider, int chunkX, int chunkZ, int minY, int maxY, double minFraction) {
        Chunk chunk = provider.getLoadedChunk(chunkX, chunkZ);
        if (chunk != null) {
            if (chunk.isTerrainPopulated()) return null;
        } else if (provider.isChunkGeneratedAt(chunkX, chunkZ)) {
            return null;
        } else {
            try {
                // Throwaway chunk: terrain and carvers only, it is not added to the world, not populated and not saved
                chunk = provider.chunkGenerator.generateChunk(chunkX, chunkZ);
            } catch (Exception e) {
                EagleMixins.LOGGER.warn("Better Stronghold: could not check chunk {}, {} for solid ground", chunkX, chunkZ, e);
                return null;
            }
            if (chunk == null) return null;
        }

        int total = 0, solid = 0;
        for (int x = 0; x < 16; x += 2) for (int z = 0; z < 16; z += 2) for (int y = Math.max(0, minY); y <= Math.min(255, maxY); y += 2) {
            IBlockState state = chunk.getBlockState(x, y, z);
            total++;
            if (state.getMaterial().isSolid()) solid++;
        }
        return total > 0 && solid >= total * minFraction;
    }
}
