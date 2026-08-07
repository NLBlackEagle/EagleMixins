package eaglemixins.mixin.vanilla;

import com.google.common.collect.Multimap;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Shadow public abstract Item getItem();

    @Unique private boolean eaglemixins$alreadyGettingAttributes = false;

    @Inject(
            method = "getAttributeModifiers",
            at = @At(value = "INVOKE", target = "Lcom/google/common/collect/HashMultimap;create()Lcom/google/common/collect/HashMultimap;", remap = false)
    )
    private void eagleMixins_itemStack_getAttributeModifiers(
            EntityEquipmentSlot slot,
            CallbackInfoReturnable<Multimap<String, AttributeModifier>> cir,
            @Share("hasCustomAttributes") LocalBooleanRef hasCustomAttributes
    ){
        hasCustomAttributes.set(true);
    }

    @ModifyReturnValue(method = "getAttributeModifiers", at = @At("RETURN"))
    private Multimap<String, AttributeModifier> eagleMixins_itemStack_getAttributeModifiers(
            Multimap<String, AttributeModifier> map,
            @Local(argsOnly = true) EntityEquipmentSlot equipmentSlot,
            @Share("hasCustomAttributes") LocalBooleanRef hasCustomAttributes
    ) {
        // put the item-inherent attributes in - no matter what
        if (hasCustomAttributes.get() && !eaglemixins$alreadyGettingAttributes) {
            // But since DDD doesn't expect Item.getAttributeModifiers to be called when there's custom attributes, we have to stop a potential StackOverflow here
            eaglemixins$alreadyGettingAttributes = true;
            map.putAll(this.getItem().getAttributeModifiers(equipmentSlot, (ItemStack) (Object) this));
            eaglemixins$alreadyGettingAttributes = false;
        }
        return map;
    }
}
