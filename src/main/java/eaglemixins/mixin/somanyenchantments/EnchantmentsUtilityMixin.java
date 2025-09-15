package eaglemixins.mixin.somanyenchantments;

import com.llamalad7.mixinextras.sugar.Local;
import com.shultrea.rin.enchantments.weapon.EnchantmentDisarmament;
import eaglemixins.util.DisarmingUtility;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(EnchantmentDisarmament.class)
public class EnchantmentsUtilityMixin {
    @ModifyArg(
            method = "onLivingAttackEvent",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityLivingBase;entityDropItem(Lnet/minecraft/item/ItemStack;F)Lnet/minecraft/entity/item/EntityItem;")
    )
    private ItemStack stopDisarmingGoodStuff(ItemStack stack, @Local(argsOnly = true) LivingAttackEvent event) {
        if (!DisarmingUtility.isAllowedToDisarm(stack, event.getEntityLiving()))
            return ItemStack.EMPTY;
        else
            return stack;
    }
}
