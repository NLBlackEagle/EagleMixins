package eaglemixins.mixin.vanilla;

import com.google.common.collect.Multimap;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import eaglemixins.config.ForgeConfigHandler;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Collection;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Shadow public abstract boolean hasTagCompound();
    @Shadow private NBTTagCompound stackTagCompound;
    @Shadow public abstract Item getItem();

    @ModifyReturnValue(
            method = "getAttributeModifiers",
            at = @At("RETURN")
    )
    Multimap<String, AttributeModifier> eagleMixins_itemStack_getAttributeModifiers(Multimap<String, AttributeModifier> map, @Local(argsOnly = true) EntityEquipmentSlot equipmentSlot) {
        if (this.hasTagCompound() && this.stackTagCompound.hasKey("AttributeModifiers", 9))
            map.putAll(this.getItem().getAttributeModifiers(equipmentSlot, (ItemStack) (Object) this));
        return map;
    }

    @Redirect(
            method = "writeToNBT",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/NBTTagCompound;setTag(Ljava/lang/String;Lnet/minecraft/nbt/NBTBase;)V")
    )
    void eagleMixins_itemStack_writeToNBT(NBTTagCompound tagCompound, String key, NBTBase value) {
        tagCompound.setTag(key, value);  //Default behavior
        if (!ForgeConfigHandler.server.removeOldAttributes) return;
        if (!key.equals("tag")) return;
        NBTTagCompound stackCompound = (NBTTagCompound) value;
        if (!stackCompound.hasKey("AttributeModifiers", 9)) return;

        Multimap<String, AttributeModifier> attributeModifiers = getItem().getItemAttributeModifiers(EntityEquipmentSlot.MAINHAND);
        Double vanillaAtkDmg = eagleMixins$getVanillaValue(attributeModifiers, "generic.attackDamage");
        Double vanillaAtkSpd = eagleMixins$getVanillaValue(attributeModifiers, "generic.attackSpeed");

        NBTTagList attrModNBTList = stackCompound.getTagList("AttributeModifiers", 10);
        if (vanillaAtkDmg != null) attrModNBTList = eagleMixins$removeOldAttribute(attrModNBTList, "generic.attackDamage", "attackDamage", vanillaAtkDmg + 1.0);
        if (vanillaAtkSpd != null) attrModNBTList = eagleMixins$removeOldAttribute(attrModNBTList, "generic.attackSpeed", "attackSpeed", vanillaAtkSpd + 4.0);
        stackCompound.setTag("AttributeModifiers", attrModNBTList);
        tagCompound.setTag(key, stackCompound);
        this.stackTagCompound = stackCompound;
    }

    @Unique
    private Double eagleMixins$getVanillaValue(Multimap<String, AttributeModifier> attributeModifiers, String attributeName){
        Collection<AttributeModifier> vanillaAtkDmgMods = attributeModifiers.get(attributeName);
        for (AttributeModifier mod : vanillaAtkDmgMods) {
            if (mod.getName().equals("Weapon modifier") || mod.getName().equals("Tool modifier")) {
                return mod.getAmount();
            }
        }
        return null;
    }

    @Unique
    private NBTTagList eagleMixins$removeOldAttribute(NBTTagList attrModNBTList, String attributeName, String oldModName, double vanillaValue){
        int indexToRemove = -1;

        for (int i = 0; i < attrModNBTList.tagCount(); ++i) {
            NBTTagCompound nbttagcompound = attrModNBTList.getCompoundTagAt(i);
            AttributeModifier modifier = SharedMonsterAttributes.readAttributeModifierFromNBT(nbttagcompound);

            //Valid modifier
            if (modifier == null || modifier.getID().getLeastSignificantBits() == 0L || modifier.getID().getMostSignificantBits() == 0L) continue;

            //Correct attribute
            if (!nbttagcompound.getString("AttributeName").equals(attributeName)) continue;

            //Correct name
            if (!modifier.getName().equals(oldModName)) continue;

            //Correct operation (addition)
            if (modifier.getOperation() != 0) continue;

            //Amount very close to vanilla value (+-0.01 for floating point inaccuracy)
            if (Math.abs(modifier.getAmount() - vanillaValue) < 0.01) {
                indexToRemove = i;
                break;
            }
        }

        //Remove modifier
        if(indexToRemove != -1)
            attrModNBTList.removeTag(indexToRemove);

        return attrModNBTList;
    }
}
