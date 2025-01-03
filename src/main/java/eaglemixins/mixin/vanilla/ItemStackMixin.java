package eaglemixins.mixin.vanilla;

import com.google.common.collect.Multimap;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Shadow public abstract boolean hasTagCompound();
    @Shadow private NBTTagCompound stackTagCompound;
    @Shadow public abstract Item getItem();

    @ModifyReturnValue(
            method = "getAttributeModifiers",
            at = @At("RETURN")
    )
    Multimap<String, AttributeModifier> eagleMixins_itemStack_getAttributeModifiers_return(Multimap<String, AttributeModifier> original, @Local(argsOnly = true) EntityEquipmentSlot equipmentSlot) {
        if (this.hasTagCompound() && this.stackTagCompound.hasKey("AttributeModifiers", 9))
            original.putAll(this.getItem().getAttributeModifiers(equipmentSlot, (ItemStack) (Object) this));
        return original;
    }
}
