package eaglemixins.mixin.vanilla.pathfollow;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import eaglemixins.config.ForgeConfigHandler;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Performance: limit how far ahead PathNavigate#pathFollow looks for a shortcut each tick.
 * <p>
 * Vanilla walks backwards from the end of the path (or the first height change) to the current
 * node and calls isDirectPathBetweenPoints for each node until one succeeds. That raytrace is
 * expensive (scans every block along the line, times the entity size) and on long, flat paths it
 * runs for dozens of nodes every tick per entity.
 * <p>
 * By capping both getCurrentPathLength() reads inside pathFollow to currentIndex + lookahead,
 * the shortcut search only considers the next N nodes. The path itself is never changed. Since
 * the window starts at the current index and the index jumps forward when a shortcut is found,
 * the window slides along every tick and converges to the same node vanilla would pick within a
 * couple of ticks.
 */
@Mixin(PathNavigate.class)
public abstract class PathNavigateMixin_PathFollowLookahead {

    @Shadow protected Path currentPath;

    @ModifyExpressionValue(
            method = "pathFollow",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/pathfinding/Path;getCurrentPathLength()I")
    )
    private int eaglemixins_capPathFollowLookahead(int length) {
        int lookahead = ForgeConfigHandler.server.pathFollowLookahead;
        if (lookahead <= 0 || this.currentPath == null) return length;
        return Math.min(length, this.currentPath.getCurrentPathIndex() + lookahead);
    }
}
