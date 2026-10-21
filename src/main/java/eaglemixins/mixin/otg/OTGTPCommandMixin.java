package eaglemixins.mixin.otg;

import com.pg85.otg.forge.commands.BaseCommand;
import com.pg85.otg.forge.commands.TPCommand;
import eaglemixins.handlers.OTGBiomeSearchHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.command.ICommandSender;

import java.util.List;

/**
 * Replaces OTG's /otg tp biome search (one huge synchronous findBiomePosition call with a fixed 8000 block range)
 * with a nearest-first, tile based search spread over multiple ticks, and adds an optional radius argument.
 */
@Mixin(TPCommand.class)
public abstract class OTGTPCommandMixin {

    @Inject(method = "<init>", at = @At("RETURN"), remap = false)
    private void eaglemixins$updateUsage(CallbackInfo ci) {
        OTGBaseCommandAccessor accessor = (OTGBaseCommandAccessor) (Object) this;
        accessor.eaglemixins$setUsage("tp <biome/dimension name or id> [radius] [-p player]");
        accessor.eaglemixins$setDescription("Attempt to teleport to the nearest target biome within the given radius in blocks, or to the target dimension.");
    }

    @Inject(method = "onCommand", at = @At("HEAD"), cancellable = true, remap = false)
    private void eaglemixins$onCommand(ICommandSender sender, List<String> args, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(OTGBiomeSearchHandler.onTpCommand((BaseCommand) (Object) this, sender, args));
    }
}
