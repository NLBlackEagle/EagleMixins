package eaglemixins.mixin.setbonus;

import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Method;

// SetBonus ships its own copy of this class (inside the mezz.jei package) compiled against an older JEI,
// where PrefixedSearchTree.getMode() returned Config$SearchMode. Newer JEI (4.24+) returns mezz.jei.config.SearchMode,
// so the original needsReload throws NoSuchMethodError every time set bonus data syncs to the client.
// This replaces the check with a reflective one that works with both JEI versions.
@Mixin(targets = "mezz.jei.ingredients.ThreadedJEITooltipReloader", remap = false)
public abstract class ThreadedJEITooltipReloaderMixin {
    @Unique private static Method eaglemixins$getMode = null;

    @Inject(method = "needsReload", at = @At("HEAD"), cancellable = true, remap = false)
    private static void eaglemixins_setbonus_needsReload(Char2ObjectMap<?> prefixedSearchTrees, CallbackInfoReturnable<Boolean> cir) {
        //Original: return true if the tooltip search tree ('#' prefix) exists and its search mode isn't DISABLED
        Object tooltipTree = prefixedSearchTrees.get('#');
        if (tooltipTree == null) {
            cir.setReturnValue(false);
            return;
        }
        try {
            if (eaglemixins$getMode == null) eaglemixins$getMode = tooltipTree.getClass().getMethod("getMode");
            Object mode = eaglemixins$getMode.invoke(tooltipTree);
            cir.setReturnValue(mode != null && !"DISABLED".equals(((Enum<?>) mode).name()));
        } catch (Exception e) {
            //Can't determine search mode, skip the reload instead of erroring
            cir.setReturnValue(false);
        }
    }
}
