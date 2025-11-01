package eaglemixins.mixin.vc;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import noppes.vc.VCItems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VCItems.class)
public abstract class VCItemsMixin {
	
	@Inject(
			method = "registerItems",
			at = @At(value = "INVOKE", target = "Lnoppes/vc/VCItems;register(Lnet/minecraftforge/event/RegistryEvent$Register;Lnet/minecraft/item/Item;)V", ordinal = 8),
			remap = false
	)
	private void eaglemixins_variedCommoditiesVCItems_registerItems(RegistryEvent.Register<Item> event, CallbackInfo ci, @Local(name = "gem_ruby") Item gem_ruby) {
		gem_ruby.setTranslationKey("gem_ruby_vc");
	}
}