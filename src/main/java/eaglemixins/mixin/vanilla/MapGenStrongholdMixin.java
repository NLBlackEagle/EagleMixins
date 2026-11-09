package eaglemixins.mixin.vanilla;

import eaglemixins.config.ForgeConfigHandler;
import eaglemixins.config.folders.StrongholdConfig;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.gen.structure.MapGenStronghold;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Pushes the stronghold rings outward by a fixed offset instead of scaling them,
 * so strongholds are absent near spawn but as common as configured beyond it.
 */
@Mixin(MapGenStronghold.class)
public abstract class MapGenStrongholdMixin {

    @Shadow
    private double distance;

    @Shadow
    private int spread;

    @Shadow
    private ChunkPos[] structureCoords;

    @Inject(method = "generatePositions", at = @At("HEAD"))
    private void eaglemixins$applyStrongholdConfig(CallbackInfo ci) {
        StrongholdConfig cfg = ForgeConfigHandler.strongholds;
        if (cfg.spread > 0) this.spread = cfg.spread;
        if (cfg.count > 0 && this.structureCoords.length != cfg.count) this.structureCoords = new ChunkPos[cfg.count];
    }

    /**
     * Ring radius (in chunks) is 4 * distance + ring * 6 * distance + random. Raising the 4 by offset / distance
     * moves every ring outward by the offset while keeping the spacing between rings.
     */
    @ModifyConstant(method = "generatePositions", constant = @Constant(doubleValue = 4.0D))
    private double eaglemixins$offsetRings(double original) {
        return original + ForgeConfigHandler.strongholds.ringOffset / 16.0D / this.distance;
    }
}
