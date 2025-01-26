package eaglemixins.handlers;

import eaglemixins.EagleMixins;
import eaglemixins.config.ForgeConfigHandler;
import eaglemixins.util.Ref;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class EntityRenameCancelHandler {

    @SubscribeEvent
    public static void onPlayerInteract(PlayerInteractEvent.EntityInteract event) {
        Entity target = event.getTarget();
        if (target == null) return;
        ResourceLocation entityId = EntityList.getKey(target);
        if(entityId == null) return;

        ItemStack usedItem = event.getItemStack();
        if (usedItem == ItemStack.EMPTY) return;
        if (!usedItem.getItem().equals(Items.NAME_TAG)) return;
        String nameOnNameTag = usedItem.getDisplayName();
        String targetCustomName = target.getName();

        if (entityId.equals(Ref.playerBossReg)) {
            for (String name : ForgeConfigHandler.server.blackListEntitiesNameChangePlayerbosses) {
                if (targetCustomName.contains(name) || nameOnNameTag.contains(name)) {
                    event.setCanceled(true);
                    return;
                }
            }
        } else if (entityId.getNamespace().equals(Ref.SRPMODID)) {
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
