package eaglemixins.mixin.otg;

import com.pg85.otg.forge.generator.structure.OTGStrongholdGen;
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
 * OTG has its own copy of the vanilla stronghold placement, see MapGenStrongholdMixin.
 */
@Mixin(OTGStrongholdGen.class)
public abstract class OTGStrongholdGenMixin extends MapGenStronghold {
    @Shadow(remap = false)
    private double distance;
    @Shadow(remap = false)
    private int spread;
    @Shadow(remap = false)
    private ChunkPos[] structureCoords;

    @Inject(method = "generatePositions", at = @At("HEAD"), remap = false)
    private void eaglemixins$applyStrongholdConfig(CallbackInfo ci) {
        StrongholdConfig cfg = ForgeConfigHandler.strongholds;
        if (cfg.spread > 0) this.spread = cfg.spread;
        if (cfg.count > 0 && this.structureCoords.length != cfg.count) this.structureCoords = new ChunkPos[cfg.count];
    }

    @ModifyConstant(method = "generatePositions", constant = @Constant(doubleValue = 4.0D), remap = false)
    private double eaglemixins$offsetRings(double original) {
        return original + ForgeConfigHandler.strongholds.ringOffset / 16.0D / this.distance;
    }
}
