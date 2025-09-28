package eaglemixins.mixin.nuclearcraft;

import nc.config.NCConfig;
import nc.recipe.vanilla.recipe.ShapelessArmorRadShieldingRecipe;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShapelessArmorRadShieldingRecipe.class)
public abstract class MixinRadShielding {

    @Inject(method = "matches", at = @At("HEAD"), cancellable = true)
    public void eaglemixins_allowDamagedArmorUpgradeOnly(InventoryCrafting inv, World worldIn, CallbackInfoReturnable<Boolean> cir) {
        ItemStack armor = ItemStack.EMPTY;
        ItemStack shielding = ItemStack.EMPTY;

        int nonEmptyCount = 0;
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack.isEmpty()) continue;
            nonEmptyCount++;
            if(nonEmptyCount > 2) {
                cir.setReturnValue(false);
                return;
            }

            Item item = stack.getItem();
            if (item instanceof ItemArmor) {
                armor = stack;
            } else if (item.getRegistryName() != null && item.getRegistryName().getPath().equals("rad_shielding")) {
                shielding = stack;
            }
        }

        if (!armor.isEmpty() && !shielding.isEmpty()) {
            double newResistance = NCConfig.radiation_shielding_level[shielding.getMetadata() > 2 ? 0 : shielding.getMetadata()];

            double currentResistance = 0.0;
            NBTTagCompound tags = armor.getTagCompound();
            if (tags != null)
                currentResistance = tags.getDouble("ncRadiationResistance"); //0 if not existing

            cir.setReturnValue(newResistance > currentResistance); // upgrade is valid if resistance is increasing
        }
    }

    @Inject(method = "getCraftingResult", at = @At("HEAD"), cancellable = true)
    public void eaglemixins_preserveNBT(InventoryCrafting inv, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack armor = ItemStack.EMPTY;
        ItemStack shielding = ItemStack.EMPTY;

        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack.isEmpty()) continue;

            Item item = stack.getItem();
            if (item instanceof ItemArmor) {
                armor = stack;
            } else if (item.getRegistryName() != null && item.getRegistryName().getPath().equals("rad_shielding")) {
                shielding = stack;
            }
        }

        if (!armor.isEmpty() && !shielding.isEmpty()) {
            double resistance = NCConfig.radiation_shielding_level[shielding.getMetadata() > 2 ? 0 : shielding.getMetadata()];

            ItemStack result = armor.copy();
            result.setCount(1);
            result.setItemDamage(armor.getItemDamage()); // retain damage

            NBTTagCompound tag;
            if (result.hasTagCompound()) {
                tag = result.getTagCompound().copy();
            } else {
                tag = new NBTTagCompound();
            }

            tag.setDouble("ncRadiationResistance", resistance);
            result.setTagCompound(tag);

            cir.setReturnValue(result);
        }
    }
}