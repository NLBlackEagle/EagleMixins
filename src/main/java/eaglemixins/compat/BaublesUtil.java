package eaglemixins.compat;

import baubles.api.BaublesApi;
import baubles.api.cap.IBaublesItemHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;

import java.util.Map;
import java.util.Set;

public class BaublesUtil {

    public static int isBaubleEquipped(EntityPlayer player, Item bauble) {
        return BaublesApi.isBaubleEquipped(player, bauble);
    }

    public static boolean isWearingImmunityBauble(EntityPlayer player, Potion applyingPotion, Map<Item, Set<Potion>> immunityMap) {
        IBaublesItemHandler baubles = BaublesApi.getBaublesHandler(player);
        if (baubles == null) return false;

        for (int slot = 0; slot < baubles.getSlots(); slot++) {
            ItemStack stack = baubles.getStackInSlot(slot);
            if (stack.isEmpty()) continue;

            Set<Potion> blocked = immunityMap.get(stack.getItem());
            if (blocked != null && blocked.contains(applyingPotion)) return true;
        }
        return false;
    }
}
