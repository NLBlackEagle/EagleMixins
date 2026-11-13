package eaglemixins.mixin.vanilla.pathnodecache;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import eaglemixins.util.IPathNodeTypeCache;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.entity.EntityLiving;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.WalkNodeProcessor;
import net.minecraft.world.IBlockAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Caches the per-block node type lookup of WalkNodeProcessor while a cache scope is open.
 * <p>
 * Vanilla evaluates the same block positions many times during one search: every entity-sized
 * box overlaps its neighbours, and every lookup also scans the 3x3 neighbours for danger.
 * Each lookup reads block states from the chunk. During a scope the world cannot change, so the
 * cached result is exactly what vanilla would compute and paths stay identical.
 * <p>
 * Safety: the cache is bypassed if a different IBlockAccess or a different entity shows up in the
 * same scope, and results with and without currentEntity are kept apart, because Forge's
 * Block#getAiPathNodeType hook receives that entity.
 */
@Mixin(WalkNodeProcessor.class)
public abstract class WalkNodeProcessorMixin_NodeTypeCache implements IPathNodeTypeCache {

    @Shadow(remap = false) protected EntityLiving currentEntity; // added by Forge, never obfuscated

    @Unique private int eaglemixins$cacheDepth;
    @Unique private IBlockAccess eaglemixins$cacheAccess;
    @Unique private EntityLiving eaglemixins$cacheEntity;
    @Unique private Long2ObjectOpenHashMap<PathNodeType> eaglemixins$cacheWithEntity;
    @Unique private Long2ObjectOpenHashMap<PathNodeType> eaglemixins$cacheNoEntity;

    @Override
    public void eaglemixins$beginNodeCache() {
        if (this.eaglemixins$cacheDepth++ == 0) {
            if (this.eaglemixins$cacheWithEntity == null) {
                this.eaglemixins$cacheWithEntity = new Long2ObjectOpenHashMap<>(512);
                this.eaglemixins$cacheNoEntity = new Long2ObjectOpenHashMap<>(64);
            }
            this.eaglemixins$cacheAccess = null;
            this.eaglemixins$cacheEntity = null;
        }
    }

    @Override
    public void eaglemixins$endNodeCache() {
        if (this.eaglemixins$cacheDepth > 0 && --this.eaglemixins$cacheDepth == 0) {
            this.eaglemixins$cacheAccess = null;
            this.eaglemixins$cacheEntity = null;
            this.eaglemixins$cacheWithEntity.clear();
            this.eaglemixins$cacheWithEntity.trim(512); // don't keep a huge table around after a big search
            this.eaglemixins$cacheNoEntity.clear();
            this.eaglemixins$cacheNoEntity.trim(64);
        }
    }

    @WrapMethod(method = "getPathNodeType(Lnet/minecraft/world/IBlockAccess;III)Lnet/minecraft/pathfinding/PathNodeType;")
    private PathNodeType eaglemixins$cachedNodeType(IBlockAccess access, int x, int y, int z, Operation<PathNodeType> original) {
        if (this.eaglemixins$cacheDepth == 0) return original.call(access, x, y, z);

        // Bind the scope to the first block access / entity seen; anything else bypasses the cache.
        if (this.eaglemixins$cacheAccess == null) this.eaglemixins$cacheAccess = access;
        else if (this.eaglemixins$cacheAccess != access) return original.call(access, x, y, z);

        EntityLiving entity = this.currentEntity;
        Long2ObjectOpenHashMap<PathNodeType> cache;
        if (entity == null) {
            cache = this.eaglemixins$cacheNoEntity;
        } else {
            if (this.eaglemixins$cacheEntity == null) this.eaglemixins$cacheEntity = entity;
            else if (this.eaglemixins$cacheEntity != entity) return original.call(access, x, y, z);
            cache = this.eaglemixins$cacheWithEntity;
        }

        long key = ((long) x & 0x3FFFFFFL) << 38 | ((long) y & 0xFFFL) << 26 | ((long) z & 0x3FFFFFFL);
        PathNodeType type = cache.get(key);
        if (type == null) {
            type = original.call(access, x, y, z);
            if (type != null) cache.put(key, type);
        }
        return type;
    }
}
