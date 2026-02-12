package eaglemixins.mixin.vanilla;

import com.google.common.collect.Maps;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Items;
import net.minecraft.item.ItemEnchantedBook;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(EnchantmentHelper.class)
public class NamespacedEnchantNBT {

    /* ---------------- getEnchantmentLevel ---------------- */

    @Inject(
            method = "getEnchantmentLevel",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void eaglemixins$getLevelNameAware(
            Enchantment enchantment,
            ItemStack stack,
            CallbackInfoReturnable<Integer> cir
    ) {
        if (stack.isEmpty()) {
            return;
        }
        NBTTagList list = stack.getEnchantmentTagList();
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound tag = list.getCompoundTagAt(i);
            Enchantment resolvedEnchantment = resolveEnchantmentByName(tag);
            if (resolvedEnchantment != null) {
                ResourceLocation resolved = resolvedEnchantment.getRegistryName();
                ResourceLocation target = enchantment.getRegistryName();
                if (resolved != null && resolved.equals(target)) {
                    cir.setReturnValue((int) tag.getShort("lvl"));
                    cir.cancel();
                    return;
                }
            }
        }
    }

    /* ---------------- getEnchantments ---------------- */

    @Inject(
            method = "getEnchantments",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void eaglemixins$getEnchantmentsNameAware(
            ItemStack stack,
            CallbackInfoReturnable<Map<Enchantment, Integer>> cir
    ) {
        Map<Enchantment, Integer> map = Maps.newLinkedHashMap();
        NBTTagList list = stack.getItem() == Items.ENCHANTED_BOOK
                ? ItemEnchantedBook.getEnchantments(stack)
                : stack.getEnchantmentTagList();

        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound tag = list.getCompoundTagAt(i);
            Enchantment ench = resolveEnchantmentByName(tag);
            if (ench == null) {
                ench = Enchantment.getEnchantmentByID(tag.getShort("id"));
            }
            if (ench != null) {
                map.put(ench, (int) tag.getShort("lvl"));
            }
        }

        cir.setReturnValue(map);
        cir.cancel();
    }

    /* ---------------- applyEnchantmentModifier ---------------- */

    @Inject(
            method = "applyEnchantmentModifier",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void eaglemixins$applyModifierNameAware(
            EnchantmentHelper.IModifier modifier,
            ItemStack stack,
            CallbackInfo ci
    ) {
        if (stack.isEmpty()) {
            ci.cancel();
            return;
        }

        NBTTagList list = stack.getEnchantmentTagList();

        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound tag = list.getCompoundTagAt(i);
            Enchantment ench = resolveEnchantmentByName(tag);
            if (ench == null) {
                ench = Enchantment.getEnchantmentByID(tag.getShort("id"));
            }
            if (ench != null) {
                modifier.calculateModifier(ench, tag.getShort("lvl"));
            }
        }

        ci.cancel();
    }

    /* ---------------- shared resolver ---------------- */

    @Unique
    private static Enchantment resolveEnchantmentByName(NBTTagCompound tag) {
        if (tag.hasKey("name", Constants.NBT.TAG_STRING)) {
            String name = tag.getString("name");
            if (!name.isEmpty()) {
                try {
                    Enchantment ench = Enchantment.REGISTRY.getObject(new ResourceLocation(name));
                    if (ench != null) {
                        return ench;
                    }

                } catch (Exception ignored) {}
            }
        }
        return null;
    }
}
