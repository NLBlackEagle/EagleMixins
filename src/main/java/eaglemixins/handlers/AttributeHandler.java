package eaglemixins.handlers;

import eaglemixins.attribute.ModAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class AttributeHandler {
    @SubscribeEvent
    public static void onEntityConstruction(EntityEvent.EntityConstructing event) {
        if(!(event.getEntity() instanceof EntityPlayer)) return;
        if (event.getEntity().world == null) return;
        if (event.getEntity().world.isRemote) return;
        ((EntityPlayer) event.getEntity()).getAttributeMap().registerAttribute(ModAttributes.RADIATION_RESISTANCE);
    }
}