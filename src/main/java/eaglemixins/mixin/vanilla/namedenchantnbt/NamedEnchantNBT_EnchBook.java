package eaglemixins.mixin.vanilla.namedenchantnbt;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import eaglemixins.util.NamespacedEnchantNBTUtil;
import net.minecraft.item.ItemEnchantedBook;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemEnchantedBook.class)
public class NamedEnchantNBT_EnchBook {

    @ModifyExpressionValue(
            method = "addInformation",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/NBTTagList;getCompoundTagAt(I)Lnet/minecraft/nbt/NBTTagCompound;", ordinal = 0)
    )
    private static NBTTagCompound eaglemixins$getLevelNameAware(NBTTagCompound nbt) {
        return NamespacedEnchantNBTUtil.modifyEnchNBT(nbt);
    }
}
