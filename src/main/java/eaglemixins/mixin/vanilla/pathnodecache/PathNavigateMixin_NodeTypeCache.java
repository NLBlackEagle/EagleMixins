package eaglemixins.mixin.vanilla.pathnodecache;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import eaglemixins.util.IPathNodeTypeCache;
import net.minecraft.pathfinding.NodeProcessor;
import net.minecraft.pathfinding.PathNavigate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Opens a node type cache scope for one pathFollow call. All shortcut raytraces
 * (isDirectPathBetweenPoints) of that tick share it, and they overlap heavily.
 */
@Mixin(PathNavigate.class)
public abstract class PathNavigateMixin_NodeTypeCache {

    @Shadow protected NodeProcessor nodeProcessor;

    @WrapMethod(method = "pathFollow")
    private void eaglemixins$cacheDuringPathFollow(Operation<Void> original) {
        NodeProcessor processor = this.nodeProcessor;
        if (!(processor instanceof IPathNodeTypeCache)) {
            original.call();
            return;
        }
        IPathNodeTypeCache cache = (IPathNodeTypeCache) processor;
        cache.eaglemixins$beginNodeCache();
        try {
            original.call();
        } finally {
            cache.eaglemixins$endNodeCache();
        }
    }
}
