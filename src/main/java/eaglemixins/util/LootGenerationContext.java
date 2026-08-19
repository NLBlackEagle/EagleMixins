package eaglemixins.util;

import com.tmtravlr.qualitytools.config.ConfigLoader;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

public class LootGenerationContext {

    private static final ThreadLocal<Deque<ResourceLocation>> stack =
            ThreadLocal.withInitial(ArrayDeque::new);

    public static void push(ResourceLocation table) {
        stack.get().push(table);
    }

    @Nullable
    public static ResourceLocation pop() {
        Deque<ResourceLocation> s = stack.get();
        if (!s.isEmpty())
            return s.pop();
        return null;
    }

    public static void clear() {
        stack.get().clear();
    }

    public static Set<String> getUniqueStack() {
        Set<String> unique = new HashSet<>();
        for (ResourceLocation loc : stack.get())
            unique.add(loc.toString()); // sets only contain unique elements
        return unique;
    }
    
    public static final String EAGLE_COMPOUND = "eaglemixins";
    public static final String LOOTTABLE_LIST = "LootTable";

    public static void addTableNBT(ItemStack stack) {
        if (stack.isEmpty()) return;
        if (!ConfigLoader.allowStackableItems && stack.getItem().getItemStackLimit(stack) > 1) // Only unstackables (if QT config says so)
            return;

        NBTTagCompound tag = stack.getOrCreateSubCompound(EAGLE_COMPOUND);

        NBTTagList list = new NBTTagList();
        for (String tableId : LootGenerationContext.getUniqueStack())
            list.appendTag(new NBTTagString(tableId));

        tag.setTag(LOOTTABLE_LIST, list);
    }

    public static Set<String> getTablesFromStack(ItemStack stack) {
        Set<String> tables = new HashSet<>();

        if (!stack.hasTagCompound()) return tables;

        NBTTagCompound eagleTag = stack.getSubCompound(EAGLE_COMPOUND);
        if (eagleTag == null || !eagleTag.hasKey(LOOTTABLE_LIST, Constants.NBT.TAG_LIST))
            return tables;

        NBTTagList list = eagleTag.getTagList(LOOTTABLE_LIST, Constants.NBT.TAG_STRING);
        for (int i = 0; i < list.tagCount(); i++)
            tables.add(list.getStringTagAt(i));

        return tables;
    }

    public static void removeTableNBT(ItemStack stack) {
        if (stack.isEmpty()) return;

        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) return;

        if (tag.hasKey(EAGLE_COMPOUND)) {
            NBTTagCompound cmp = tag.getCompoundTag(EAGLE_COMPOUND);
            if(cmp.hasKey(LOOTTABLE_LIST))
                cmp.removeTag(LOOTTABLE_LIST);
            if(cmp.isEmpty())
                tag.removeTag(EAGLE_COMPOUND);
            if (tag.isEmpty())
                stack.setTagCompound(null);
        }
    }
}