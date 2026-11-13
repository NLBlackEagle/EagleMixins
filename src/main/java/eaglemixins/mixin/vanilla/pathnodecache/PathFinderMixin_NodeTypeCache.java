package eaglemixins.mixin.vanilla.pathnodecache;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import eaglemixins.util.IPathNodeTypeCache;
import net.minecraft.entity.EntityLiving;
import net.minecraft.pathfinding.NodeProcessor;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.world.IBlockAccess;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/** Opens a node type cache scope for the duration of one A* search. */
@Mixin(PathFinder.class)
public abstract class PathFinderMixin_NodeTypeCache {

    @Shadow @Final private NodeProcessor nodeProcessor;

    @WrapMethod(method = "findPath(Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/entity/EntityLiving;DDDF)Lnet/minecraft/pathfinding/Path;")
    private Path eaglemixins$cacheDuringSearch(IBlockAccess world, EntityLiving entity, double x, double y, double z, float maxDistance, Operation<Path> original) {
        if (!(this.nodeProcessor instanceof IPathNodeTypeCache)) return original.call(world, entity, x, y, z, maxDistance);
        IPathNodeTypeCache cache = (IPathNodeTypeCache) this.nodeProcessor;
        cache.eaglemixins$beginNodeCache();
        try {
            return original.call(world, entity, x, y, z, maxDistance);
        } finally {
            cache.eaglemixins$endNodeCache();
        }
    }
}
