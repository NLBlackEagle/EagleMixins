package eaglemixins.mixin.otg;

import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.generator.ObjectSpawner;
import com.pg85.otg.util.ChunkCoordinate;
import eaglemixins.util.IDeferredStronghold;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

/**
 * Places the stronghold remembered by ForgeWorldDeferStrongholdMixin at the very end of OTG's chunk population,
 * after all resources, BO3s/BO4s and ReplacedBlocks, so nothing of OTG generates into it anymore.
 */
@Mixin(value = ObjectSpawner.class, remap = false)
public abstract class ObjectSpawnerDeferStrongholdMixin {
    @Shadow @Final private LocalWorld world;
    @Shadow @Final private Random rand;

    @Inject(
            method = "doPopulate",
            at = @At(value = "INVOKE", target = "Lcom/pg85/otg/OTG;firePopulationEndEvent(Lcom/pg85/otg/common/LocalWorld;Ljava/util/Random;ZLcom/pg85/otg/util/ChunkCoordinate;)V")
    )
    private void eaglemixins$placeDeferredStronghold(ChunkCoordinate chunkCoord, CallbackInfo ci) {
        if (this.world instanceof IDeferredStronghold) ((IDeferredStronghold) this.world).eaglemixins$placeDeferredStronghold(this.rand);
    }
}
