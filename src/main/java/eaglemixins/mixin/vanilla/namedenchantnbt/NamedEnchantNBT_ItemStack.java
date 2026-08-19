package eaglemixins.mixin.vanilla.namedenchantnbt;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import eaglemixins.util.NamespacedEnchantNBTUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(ItemStack.class)
public class NamedEnchantNBT_ItemStack {

    // intercepts before reading "id",
    // checks if the enchant entry instead has "name"
    // and modifies the name to its corresponding integer id
    // which is safe once it's an ItemStack in a world!
    @ModifyExpressionValue(
            method = {"getTooltip"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/NBTTagList;getCompoundTagAt(I)Lnet/minecraft/nbt/NBTTagCompound;"),
            //just to be extra sure, even though there is only one target in the code. you never know what ppl do with ASM
            slice = @Slice(
                    from = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getEnchantmentTagList()Lnet/minecraft/nbt/NBTTagList;"),
                    to = @At(value = "INVOKE", target = "Lnet/minecraft/enchantment/Enchantment;getTranslatedName(I)Ljava/lang/String;")
            )
    )
    private static NBTTagCompound eaglemixins$getLevelNameAware(NBTTagCompound nbt) {
        return NamespacedEnchantNBTUtil.modifyEnchNBT(nbt);
    }
}
