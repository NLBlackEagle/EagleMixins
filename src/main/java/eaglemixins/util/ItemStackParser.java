package eaglemixins.util;

import eaglemixins.EagleMixins;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

/** Parses config item strings: <modid:item>[@damage] [{nbt}], e.g. "minecraft:bow@0 {ench:[{id:48s,lvl:5s}]}". */
public final class ItemStackParser {

    /** @return the stack, or ItemStack.EMPTY for an empty string or an unknown item (logged) */
    public static ItemStack parse(String text, String context) {
        text = text.trim();
        if (text.isEmpty()) return ItemStack.EMPTY;

        String nbt = null;
        int brace = text.indexOf('{');
        if (brace >= 0) {
            nbt = text.substring(brace);
            text = text.substring(0, brace).trim();
        }
        int damage = 0;
        int at = text.indexOf('@');
        if (at >= 0) {
            try {
                damage = Integer.parseInt(text.substring(at + 1).trim());
            } catch (NumberFormatException e) {
                EagleMixins.LOGGER.error("Invalid damage value in \"{}\" ({})", text, context);
            }
            text = text.substring(0, at).trim();
        }

        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(text));
        if (item == null) {
            EagleMixins.LOGGER.warn("Unknown item \"{}\" ({})", text, context);
            return ItemStack.EMPTY;
        }
        ItemStack stack = new ItemStack(item, 1, damage);
        if (nbt != null) {
            try {
                stack.setTagCompound(JsonToNBT.getTagFromJson(nbt));
            } catch (NBTException e) {
                EagleMixins.LOGGER.error("Invalid NBT for \"{}\" ({}): {}", text, context, e.getMessage());
            }
        }
        return stack;
    }

    /** "#RRGGBB" or "RRGGBB" to an int color, white when invalid. */
    public static int parseColor(String text) {
        try {
            return Integer.parseInt(text.trim().replace("#", ""), 16);
        } catch (NumberFormatException e) {
            return 0xFFFFFF;
        }
    }

    private ItemStackParser() {}
}
