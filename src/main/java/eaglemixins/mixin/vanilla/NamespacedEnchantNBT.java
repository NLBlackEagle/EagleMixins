package eaglemixins.mixin.vanilla;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EnchantmentHelper.class)
public class NamespacedEnchantNBT {

    // intercepts before reading "id",
    // checks if the enchant entry instead has "name"
    // and modifies the name to its corresponding integer id
    // which is safe once it's an ItemStack in a world!
    @ModifyExpressionValue(
            method = {"getEnchantmentLevel", "getEnchantments", "applyEnchantmentModifier"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/NBTTagList;getCompoundTagAt(I)Lnet/minecraft/nbt/NBTTagCompound;", ordinal = 0)
    )
    private static NBTTagCompound eaglemixins$getLevelNameAware(NBTTagCompound nbt) {
        if(!nbt.hasKey("name")) return nbt;

        Enchantment ench = Enchantment.getEnchantmentByLocation(nbt.getString("name"));
        if(ench == null) return nbt;

        //Swap name for id
        nbt.setInteger("id", Enchantment.getEnchantmentID(ench));
        nbt.removeTag("name");
        return nbt;
    }
}
