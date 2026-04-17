package eaglemixins.util;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.asm.mixin.Unique;

public class QualityToolsNBTRemover {

    @Unique
    public static void eaglemixins$clean(ItemStack stack) {
        if (stack.isEmpty()) return;

        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) return;

        if (tag.hasKey("eaglemixins")) {
            tag.removeTag("eaglemixins");

            if (tag.isEmpty()) {
                stack.setTagCompound(null);
            }
        }
    }
}
