package eaglemixins.handlers;

import com.dhanantry.scapeandrunparasites.entity.ai.misc.EntityParasiteBase;
import com.lothrazar.playerbosses.EntityPlayerBoss;
import eaglemixins.config.ForgeConfigHandler;
import net.minecraft.entity.Entity;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class EntityRenameCancelHandler {

    @SubscribeEvent
    public static void onPlayerInteract(PlayerInteractEvent.EntityInteract event) {
        Entity target = event.getTarget();
        if (target == null) return;

        ItemStack usedItem = event.getItemStack();
        if (usedItem == ItemStack.EMPTY) return;
        if (!usedItem.getItem().equals(Items.NAME_TAG)) return;
        String nameOnNameTag = usedItem.getDisplayName();
        String targetCustomName = target.getName();

        if (target instanceof EntityPlayerBoss) {
            for (String name : ForgeConfigHandler.server.blackListEntitiesNameChangePlayerbosses) {
                if (targetCustomName.contains(name) || nameOnNameTag.contains(name)) {
                    event.setCanceled(true);
                    return;
                }
            }
        } else if (target instanceof EntityParasiteBase) {
            for (String name : ForgeConfigHandler.server.blackListEntitiesNameChangeParasite) {
                if (targetCustomName.contains(name) || nameOnNameTag.contains(name)) {
                    event.setCanceled(true);
                    return;
                }
            }
        }
        //Fix: Do this for all mobs, not for all except playerbosses+parasites
        for (String name : ForgeConfigHandler.server.blackListEntitiesNameChangeAny) {
            if (targetCustomName.contains(name) || nameOnNameTag.contains(name)) {
                event.setCanceled(true);
                return;
            }
        }
    }
}
