package eaglemixins.mixin.otg;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.pg85.otg.customobjects.bo3.BO3Config;
import com.pg85.otg.customobjects.bo3.bo3function.BO3BlockFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BO3Config.class)
public abstract class CacheBlockFunctionsForBO3Config {
    @Shadow(remap = false) protected boolean rotateRandomly;
    @Unique public BO3BlockFunction[] eaglemixins$cache = null;

    @Inject(
            method = "getBlocks",
            at = @At("HEAD"),
            remap = false,
            cancellable = true
    )
    private void eaglemixins_returnCache(int rotation, CallbackInfoReturnable<BO3BlockFunction[]> cir){
        if(!this.rotateRandomly && rotation == 0 && eaglemixins$cache != null) cir.setReturnValue(eaglemixins$cache);
    }

    @ModifyReturnValue(
            method = "getBlocks",
            at = @At("RETURN"),
            remap = false
    )
    private BO3BlockFunction[] eaglemixins_setCache(BO3BlockFunction[] original, int rotation){
        if(!this.rotateRandomly && rotation == 0 && eaglemixins$cache == null) eaglemixins$cache = original;
        return original;
    }
}
