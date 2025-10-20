package eaglemixins.handlers;

import eaglemixins.init.RadiationResistanceRegistry;
import nc.capability.radiation.entity.IEntityRads;
import nc.radiation.RadiationHelper;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class RadiationResistanceApplier {

    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (event.getWorld().isRemote) return;
        if (!(event.getEntity() instanceof EntityLivingBase)) return;

        EntityLivingBase living = (EntityLivingBase) event.getEntity();
        ResourceLocation id = EntityList.getKey(living);
        if (id == null) return;

        double resistance = RadiationResistanceRegistry.get(id);
        if (resistance <= 0.0D) return;

        IEntityRads rads = RadiationHelper.getEntityRadiation(living);
        if (rads == null) return;

        //we're technically overwriting existing internal rad res here
        // but this system by default is only used by players consuming rad resistant stuff
        // and this code wouldn't run for players anyway (returns at EntityList.getKey)
        rads.setInternalRadiationResistance(resistance);
    }
}
