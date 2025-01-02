package eaglemixins.handlers;

import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class HealthValidationHandler {

    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent event) {
        validateHealth(event);
        if (Float.isNaN(event.getAmount()) || event.getAmount() == Float.NEGATIVE_INFINITY) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        validateHealth(event);
    }

    private void validateHealth(LivingEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        if (entity.world.isRemote) {
            return;
        }
        float health = entity.getHealth();
        if (Float.isNaN(health) || Float.isInfinite(health) || health < 0F) {
            entity.setHealth(0F);
        }
    }
}
