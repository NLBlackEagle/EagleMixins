package eaglemixins.handlers;

import eaglemixins.init.ModItems;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Guaranteed drops for the Asmodeus crafting items from Fish's Undead Rising mobs. */
public class AsmodeusDropsHandler {

    private static final ResourceLocation ENIGMOTH = new ResourceLocation("mod_lavacow", "enigmoth");
    private static final ResourceLocation GHOSTRAY = new ResourceLocation("mod_lavacow", "ghostray");

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        if (entity.world.isRemote) return;
        ResourceLocation id = EntityList.getKey(entity);
        if (id == null) return;

        Item drop = null;
        if (id.equals(ENIGMOTH)) drop = ModItems.ENIGMOTH_EYE;
        else if (id.equals(GHOSTRAY)) drop = ModItems.GHOSTLY_GILLS;
        if (drop == null) return;

        EntityItem item = new EntityItem(entity.world, entity.posX, entity.posY, entity.posZ, new ItemStack(drop));
        item.setDefaultPickupDelay();
        event.getDrops().add(item);
    }
}
