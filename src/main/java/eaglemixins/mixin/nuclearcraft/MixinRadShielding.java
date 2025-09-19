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
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(ShapelessArmorRadShieldingRecipe.class)
public abstract class MixinRadShielding {

    @Unique
    double[] eagleMixins$levels = NCConfig.radiation_shielding_level;

    @Inject(method = "matches", at = @At("HEAD"), cancellable = true)
    public void eagle$allowDamagedArmorUpgradeOnly(InventoryCrafting inv, World worldIn, CallbackInfoReturnable<Boolean> cir) {
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
            double newResistance;
            switch (shielding.getMetadata()) {
                case 2: newResistance = eagleMixins$levels[2]; break;
                case 1: newResistance = eagleMixins$levels[1]; break;
                default: newResistance = eagleMixins$levels[0]; break;
            }

            double currentResistance = 0.0;
            if (armor.hasTagCompound()) {
                assert armor.getTagCompound() != null;
                if (armor.getTagCompound().hasKey("ncRadiationResistance")) {
                    currentResistance = armor.getTagCompound().getDouble("ncRadiationResistance");
                }
            }

            if (newResistance > currentResistance) {
                cir.setReturnValue(true); // upgrade is valid
            } else {
                cir.setReturnValue(false); // same or downgrade, deny
            }
        }
    }

    @Inject(method = "getCraftingResult", at = @At("HEAD"), cancellable = true)
    public void eagle$preserveNBT(InventoryCrafting inv, CallbackInfoReturnable<ItemStack> cir) {
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
            double resistance;
            switch (shielding.getMetadata()) {
                case 2:
                    resistance = eagleMixins$levels[2];
                    break;
                case 1:
                    resistance = eagleMixins$levels[1];
                    break;
                default:
                    resistance = eagleMixins$levels[0];
                    break;
            }

            ItemStack result = armor.copy();
            result.setCount(1);
            result.setItemDamage(armor.getItemDamage()); // retain damage

            NBTTagCompound tag;
            if (result.hasTagCompound()) {
                assert result.getTagCompound() != null;
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