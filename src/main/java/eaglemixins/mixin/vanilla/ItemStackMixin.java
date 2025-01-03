package eaglemixins.mixin.vanilla;

import com.google.common.collect.Multimap;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Unique private EntityEquipmentSlot eagleMixins$equipmentSlot;

    @Shadow public abstract boolean hasTagCompound();
    @Shadow private NBTTagCompound stackTagCompound;
    @Shadow public abstract Item getItem();

    @Inject(
            method = "getAttributeModifiers",
            at = @At(value = "HEAD")
    )
    void eagleMixins_itemStack_getAttributeModifiers_head(EntityEquipmentSlot equipmentSlot, CallbackInfoReturnable<Multimap<String, AttributeModifier>> cir) {
        this.eagleMixins$equipmentSlot = equipmentSlot;
    }

    @ModifyReturnValue(
            method = "getAttributeModifiers",
            at = @At("RETURN")
    )
    Multimap<String, AttributeModifier> eagleMixins_itemStack_getAttributeModifiers_return(Multimap<String, AttributeModifier> original) {
        if (this.hasTagCompound() && this.stackTagCompound.hasKey("AttributeModifiers", 9)) {
            original.putAll(this.getItem().getAttributeModifiers(eagleMixins$equipmentSlot, (ItemStack) (Object) this));
            this.eagleMixins$equipmentSlot = null;
        }
        return original;
    }
}
