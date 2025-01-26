package eaglemixins.handlers;

import eaglemixins.util.Ref;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class RecallFlightCancelHandler {

    //Cancels Flight pot below y=70 and all uses of mirrors or recalls/wormholes in Abyssal Rift
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClick(PlayerInteractEvent.RightClickItem event) {
        EntityPlayer player = event.getEntityPlayer();
        if (player.world.isRemote) return;
        if (!Ref.entityIsInAbyssalRift(player)) return;

        ItemStack usedStack = event.getItemStack();
        Item usedItem = usedStack.getItem();

        //Flight pots
        if (usedItem instanceof ItemPotion && player.posY <= 70) {
            for (PotionEffect effect : PotionUtils.getEffectsFromStack(usedStack))
                if (effect.getPotion().getRegistryName() != null) {
                    String potionId = effect.getPotion().getRegistryName().toString();
                    if (potionId.equals("potioncore:flight") || potionId.equals("potioncore:long_flight")) {
                        player.sendStatusMessage(new TextComponentTranslation("eaglemixins.messages.potioncancel"), true);
                        player.dropItem(true);
                        event.setCanceled(true);
                        return;
                    }
                }
        }

        if (usedItem.getRegistryName() == null) return;
        String itemId = usedItem.getRegistryName().toString();

        //Mirrors
        if (itemId.equals("bountifulbaubles:magicmirror") || itemId.equals("bountifulbaubles:wormholemirror")) {
            player.sendStatusMessage(new TextComponentTranslation("eaglemixins.messages.mirrorcancel"), true);
            player.dropItem(true);
            event.setCanceled(true);
            return;
        }
        //Recall+Wormhole pots
        if (itemId.equals("bountifulbaubles:potionrecall") || itemId.equals("bountifulbaubles:potionwormhole")) {
            player.sendStatusMessage(new TextComponentTranslation("eaglemixins.messages.potioncancel"), true);
            player.dropItem(true);
            event.setCanceled(true);
        }
    }
}