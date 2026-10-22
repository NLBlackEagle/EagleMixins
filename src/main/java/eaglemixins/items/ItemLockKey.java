package eaglemixins.items;

import eaglemixins.blocks.BlockLocked;
import eaglemixins.blocks.TileEntityLock;
import eaglemixins.config.ForgeConfigHandler;
import eaglemixins.config.folders.LockedBlockConfig;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

/** One-time key that only opens locked_blocks with the same LockId NBT. Names, color and lore come from LockedBlockConfig. */
@SuppressWarnings("deprecation") // translation.I18n also works server-side, where display names end up in chat messages
public class ItemLockKey extends Item {

    public ItemLockKey() {
        setMaxStackSize(16);
        setCreativeTab(CreativeTabs.MISC);
    }

    public static String getLockId(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        return tag == null ? "" : tag.getString(TileEntityLock.TAG);
    }

    public static ItemStack createStack(Item key, String lockId) {
        ItemStack stack = new ItemStack(key);
        stack.setTagInfo(TileEntityLock.TAG, new NBTTagString(lockId));
        return stack;
    }

    public static int getColor(String lockId) {
        LockedBlockConfig.LockType type = ForgeConfigHandler.lockedblocks.getLockType(lockId);
        return type == null ? 0xFFFFFF : type.color;
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        return displayName(getLockId(stack), "eaglemixins.lock.key_name", super.getItemStackDisplayName(stack));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        LockedBlockConfig.LockType type = ForgeConfigHandler.lockedblocks.getLockType(getLockId(stack));
        if (type != null) tooltip.addAll(type.keyLore);
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (!isInCreativeTab(tab)) return;
        for (LockedBlockConfig.LockType type : ForgeConfigHandler.lockedblocks.getLockTypes()) items.add(createStack(this, type.id));
    }

    /** "<Name> Key" / "<Name> Lock" for a LockId, the plain item name when there is none. */
    static String displayName(String lockId, String formatKey, String plainName) {
        if (lockId.isEmpty()) return plainName;
        LockedBlockConfig.LockType type = ForgeConfigHandler.lockedblocks.getLockType(lockId);
        String name = type != null && !type.name.isEmpty() ? type.name : prettify(lockId);
        return I18n.translateToLocalFormatted(formatKey, name);
    }

    /** arachnid_queen -> Arachnid Queen */
    private static String prettify(String id) {
        StringBuilder sb = new StringBuilder();
        for (String word : id.split("[_ ]")) {
            if (word.isEmpty()) continue;
            if (sb.length() > 0) sb.append(' ');
            sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return sb.toString();
    }

    /** ItemBlock for locked_block, named after the LockId in its BlockEntityTag. */
    public static class ItemBlockLocked extends ItemBlock {
        public ItemBlockLocked(BlockLocked block) {
            super(block);
        }

        public static String getLockId(ItemStack stack) {
            NBTTagCompound tag = stack.getSubCompound("BlockEntityTag");
            return tag == null ? "" : tag.getString(TileEntityLock.TAG);
        }

        @Override
        public String getItemStackDisplayName(ItemStack stack) {
            return displayName(getLockId(stack), "eaglemixins.lock.lock_name", super.getItemStackDisplayName(stack));
        }

        @Override
        @SideOnly(Side.CLIENT)
        public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
            LockedBlockConfig.LockType type = ForgeConfigHandler.lockedblocks.getLockType(getLockId(stack));
            if (type != null) tooltip.addAll(type.lockLore);
        }

        @Override
        public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
            if (!isInCreativeTab(tab)) return;
            items.add(new ItemStack(this)); // no LockId, bind it in creative by right-clicking it with a key
            for (LockedBlockConfig.LockType type : ForgeConfigHandler.lockedblocks.getLockTypes()) items.add(((BlockLocked) block).createStack(type.id));
        }
    }
}
