package eaglemixins.handlers;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Set;
import java.util.Collections;
import java.util.WeakHashMap;

public class QualityToolsNBTRemover {

    private static final Set<ItemStack> CLEANUP_QUEUE =
            Collections.newSetFromMap(new WeakHashMap<>());

    public static void mark(ItemStack stack) {
        if (!stack.isEmpty()) {
            CLEANUP_QUEUE.add(stack);
        }
    }

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;

        for (ItemStack stack : CLEANUP_QUEUE) {
            NBTTagCompound tag = stack.getTagCompound();

            if (tag != null) {
                tag.removeTag("eaglemixins");

                if (tag.isEmpty()) {
                    stack.setTagCompound(null);
                }
            }
        }

        CLEANUP_QUEUE.clear();
    }
}