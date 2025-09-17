package eaglemixins.handlers;

import eaglemixins.attribute.ModAttributes;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.player.EntityPlayer;

public class AttributeHandler {
    @SubscribeEvent
    public void onEntityJoin(EntityJoinWorldEvent event) {
        if (event.getEntity() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) event.getEntity();
            if (player.world.isRemote) return;
            if (player.getEntityAttribute(ModAttributes.RADIATION_RESISTANCE) == null) {
                player.getAttributeMap().registerAttribute(ModAttributes.RADIATION_RESISTANCE);
            }
        }
    }
}