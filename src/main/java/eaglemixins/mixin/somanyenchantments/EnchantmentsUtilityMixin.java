package eaglemixins.mixin.somanyenchantments;

import com.Shultrea.Rin.Utility_Sector.EnchantmentsUtility;
import com.llamalad7.mixinextras.sugar.Local;
import eaglemixins.util.DisarmingUtility;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(EnchantmentsUtility.class)
public class EnchantmentsUtilityMixin {
    @ModifyArg(
            method = "Disarm",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityLivingBase;entityDropItem(Lnet/minecraft/item/ItemStack;F)Lnet/minecraft/entity/item/EntityItem;")
    )
    private static ItemStack stopDisarmingGoodStuff(ItemStack stack, @Local(argsOnly = true) EntityLivingBase target) {
        if (!DisarmingUtility.isAllowedToDisarm(stack, target))
            return ItemStack.EMPTY;
        else
            return stack;
    }
}
