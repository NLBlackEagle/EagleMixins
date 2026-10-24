package eaglemixins.handlers.util;

import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;

/** EntityPlayerMP remembers the random spawn point it was created at, see EntityPlayerMPMixin_SafeSpawn */
public interface SafeSpawnPlayer {
    @Nullable
    BlockPos eaglemixins$getRandomSpawn();
}
