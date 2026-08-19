package eaglemixins.util;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.nbt.NBTTagCompound;

public class NamespacedEnchantNBTUtil {
    public static NBTTagCompound modifyEnchNBT(NBTTagCompound nbt){
        if(!nbt.hasKey("name")) return nbt;

        Enchantment ench = Enchantment.getEnchantmentByLocation(nbt.getString("name"));
        if(ench == null) return nbt;

        //Swap name for id
        nbt.setInteger("id", Enchantment.getEnchantmentID(ench));
        nbt.removeTag("name");

        return nbt;
    }
}
