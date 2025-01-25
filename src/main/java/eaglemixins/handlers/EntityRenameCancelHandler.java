package eaglemixins.handlers;

import eaglemixins.EagleMixins;
import eaglemixins.config.ForgeConfigHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class EntityRenameCancelHandler {
    private static final ResourceLocation playerBossReg = new ResourceLocation("playerbosses:player_boss");

    @SubscribeEvent
    public static void onPlayerInteract(PlayerInteractEvent.EntityInteract event) {
        Entity target = event.getTarget();
        if (target == null) return;
        if (target.world.isRemote) return;
        ResourceLocation entityId = EntityList.getKey(target);
        if(entityId == null) return;

        ItemStack usedItem = event.getItemStack();
        if (usedItem == ItemStack.EMPTY) return;
        if (!usedItem.getItem().equals(Items.NAME_TAG)) return;
        String nameOnNameTag = usedItem.getDisplayName();
        String targetCustomName = target.getCustomNameTag();
        EagleMixins.LOGGER.info("NameTag says "+nameOnNameTag+", entity is named "+targetCustomName);

        //TODO: shouldn't some of these checks just check for contains instead of equals?
        // why are we doing this anyway? like do mobs named like that have special effects or what?
        // why don't we do this via NBT?
        if (entityId.equals(playerBossReg)) {
            for (String name : ForgeConfigHandler.server.blackListEntitiesNameChangePlayerbosses)
                if (name.equals(targetCustomName) || nameOnNameTag.contains(name))
                    event.setCanceled(true);
        } else if (entityId.getNamespace().equals("srparasites")) {
            for (String name : ForgeConfigHandler.server.blackListEntitiesNameChangeParasite)
                if (name.equals(targetCustomName) || nameOnNameTag.contains(name))
                    event.setCanceled(true);
        } else {
            for (String name : ForgeConfigHandler.server.blackListEntitiesNameChangeAny)
                if (name.equals(targetCustomName) || nameOnNameTag.contains(name))
                    event.setCanceled(true);
        }
    }
}
