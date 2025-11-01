package eaglemixins.mixin.reskillable;

import codersafterdark.reskillable.base.LevelLockHandler;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelLockHandler.class)
public class LevelLockHandlerMixin {

    @Inject(method = "rightClickBlock", at = @At("HEAD"), cancellable = true, remap = false)
    private static void eagleMixins_allowRightClickBlockForNCBlocks(PlayerInteractEvent.RightClickBlock event, CallbackInfo ci) {
        ResourceLocation id = event.getWorld().getBlockState(event.getPos()).getBlock().getRegistryName();
        if (id != null && "nuclearcraft".equals(id.getNamespace())) ci.cancel();
    }
}
