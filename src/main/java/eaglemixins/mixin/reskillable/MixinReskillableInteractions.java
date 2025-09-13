package eaglemixins.mixin.reskillable;

import codersafterdark.reskillable.base.LevelLockHandler;
import eaglemixins.EagleMixins;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import org.apache.logging.log4j.Level;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Debug(export = true)
@Mixin(LevelLockHandler.class)
public class MixinReskillableInteractions {

    @Inject(method = "rightClickBlock", at = @At("HEAD"), cancellable = true, remap = false)
    private static void rightClickBlockEagleMixins(PlayerInteractEvent.RightClickBlock event, CallbackInfo ci) {
        ResourceLocation id = event.getWorld().getBlockState(event.getPos()).getBlock().getRegistryName();

        EagleMixins.LOGGER.log(Level.INFO, "Resource Location {}", id);

        if (id != null) {
            if ("nuclearcraft".equals(id.getNamespace())) {

                EagleMixins.LOGGER.log(Level.INFO, "nuclearcraft Equals ID {}", id);

                if (event.isCanceled()) event.setCanceled(false);

                ci.cancel();
            }
        }
    }
}
