package eaglemixins.mixin.dregorarl;

import com.otg.worldpacker.WorldPacker;
import net.minecraftforge.fml.common.ProgressManager;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.jar.JarEntry;

@Mixin(WorldPacker.class)
public abstract class WorldPackerMixin {

    @Unique
    private ProgressManager.ProgressBar eaglemixins$bar = null;

    @Redirect(
            method = "load",
            at = @At(value = "INVOKE", target = "Ljava/util/ArrayList;iterator()Ljava/util/Iterator;"),
            remap = false
    )
    private Iterator<JarEntry> eaglemixins_dregoraRLWorldPacker_load_redirect(ArrayList<JarEntry> instance) {
        this.eaglemixins$bar = ProgressManager.push("First time setup, do not close! Creating files:", instance.size());
        return instance.iterator();
    }

    @Inject(
            method = "load",
            at = @At(value = "INVOKE", target = "Ljava/util/Iterator;next()Ljava/lang/Object;"),
            remap = false
    )
    private void eaglemixins_dregoraRLWorldPacker_load_inject0(FMLInitializationEvent event, CallbackInfo ci) {
        if(this.eaglemixins$bar != null) this.eaglemixins$bar.step("");
    }

    @Inject(
            method = "load",
            at = @At(value = "INVOKE", target = "Ljava/util/jar/JarFile;close()V"),
            remap = false
    )
    private void eaglemixins_dregoraRLWorldPacker_load_inject1(FMLInitializationEvent event, CallbackInfo ci) {
        if(this.eaglemixins$bar != null) ProgressManager.pop(this.eaglemixins$bar);
    }
}