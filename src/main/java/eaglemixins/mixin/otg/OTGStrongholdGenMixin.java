package eaglemixins.mixin.otg;

import com.pg85.otg.forge.generator.structure.OTGStrongholdGen;
import net.minecraft.world.gen.structure.MapGenStronghold;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OTGStrongholdGen.class)
public abstract class OTGStrongholdGenMixin extends MapGenStronghold
{
    @Shadow
    private double distance;
    @Shadow
    private int spread;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void eaglemixins$scaleStrongholdValues(CallbackInfo ci)
    {
        this.distance *= 8.0D;
        this.spread *= 8;
    }
}