package eaglemixins.mixin.fishsundeadrising;

import com.Fishmod.mod_LavaCow.item.ItemFishCustomWeapon;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ItemFishCustomWeapon.class)
public abstract class ItemFishCustomWeaponMixin {

    // Cancel custom sweep handling but execute dura loss
    @Redirect(
            method = "hitEntity",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getItem()Lnet/minecraft/item/Item;", ordinal = 0)
    )
    private Item eagleMixins_furScythe_hitEntity(ItemStack instance){
        return null;
    }
}
