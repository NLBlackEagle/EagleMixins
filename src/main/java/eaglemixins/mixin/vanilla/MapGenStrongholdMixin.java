package eaglemixins.mixin.vanilla;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.MapGenStronghold;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MapGenStronghold.class)
public abstract class MapGenStrongholdMixin {

    @Shadow
    private double distance;

    @Shadow
    private int spread;

    @Shadow
    private ChunkPos[] structureCoords;

    // Multiplier for scaling distance and spread
    private static final int MULTIPLIER = 8;


    /**
     * Scale stronghold ring distance and per-ring spread.
     */
    @Inject(method = "generatePositions", at = @At("HEAD"))
    private void eaglemixins$scaleStrongholdParams(CallbackInfo ci) {
        // Scale distance
        this.distance *= MULTIPLIER;

        // Scale number of strongholds per ring
        this.spread *= MULTIPLIER;

        // Safety clamp
        this.spread = Math.max(1, Math.min(this.spread, this.structureCoords.length));
    }
}