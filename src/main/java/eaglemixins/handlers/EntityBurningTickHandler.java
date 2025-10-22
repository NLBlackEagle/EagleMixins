package eaglemixins.handlers;

import com.dhanantry.scapeandrunparasites.entity.ai.misc.EntityParasiteBase;
import nc.capability.radiation.entity.IEntityRads;
import nc.radiation.RadiationHelper;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.monster.IMob;
import net.minecraft.init.MobEffects;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class EntityBurningTickHandler {

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onLivingUpdateEvent(LivingEvent.LivingUpdateEvent event) {

        //TODO: make this a potion effect instead

        if (event.getEntityLiving().world.isRemote) return;
        if (event.getEntityLiving() == null) return;
        if (!(event.getEntityLiving().isBurning())) return;
        if (event.getEntityLiving() instanceof EntityParasiteBase) return;
        if (!(event.getEntityLiving() instanceof IMob)) return;
        if (!(event.getEntity().isCreatureType(EnumCreatureType.MONSTER, true))) return;
        if (!(event.getEntityLiving().isPotionActive(MobEffects.FIRE_RESISTANCE))) return;

        IEntityRads radsCap = RadiationHelper.getEntityRadiation(event.getEntityLiving());

        if(radsCap == null) return;
        if(radsCap.getRadsPercentage() < 90) return;

        event.getEntityLiving().extinguish();
    }
}

