package eaglemixins.mixin.reskillable;

import codersafterdark.reskillable.base.LevelLockHandler;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelLockHandler.class)
public abstract class MixinReskillableBlockOverride {

    //Overwrites Reskillable's interaction lock check to bypass for NuclearCraft blocks.
    @Inject(
            method = "rightClickBlock",
            at = @At("HEAD"),
            remap = false,
            cancellable = true
    )
    private static void rightClickBlock(PlayerInteractEvent.RightClickBlock event, CallbackInfo ci) {
        ResourceLocation blockResourceLocation = event.getWorld().getBlockState(event.getPos()).getBlock().getRegistryName();
        if(blockResourceLocation == null) return;
        String modid = blockResourceLocation.getNamespace();

        if (modid.equals("cookingforblockheads") || modid.equals("nuclearcraft"))
            // Allow interaction unconditionally
            ci.cancel();
    }
}