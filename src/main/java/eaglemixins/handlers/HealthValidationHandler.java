package eaglemixins.handlers;

import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class HealthValidationHandler {
    
    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public static void onLivingHurtHighest(LivingHurtEvent event) {
        if(event.getEntityLiving() == null) return;
        if(event.getEntityLiving().world.isRemote) return;
        
        validateHealth(event.getEntityLiving());
        
        if(Float.isNaN(event.getAmount()) || event.getAmount() == Float.NEGATIVE_INFINITY) {
            event.setAmount(0.0F);
        }
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void onLivingHurtLowest(LivingHurtEvent event) {
        if(event.getEntityLiving() == null) return;
        if(event.getEntityLiving().world.isRemote) return;
        
        validateHealth(event.getEntityLiving());
        
        if(Float.isNaN(event.getAmount()) || event.getAmount() == Float.NEGATIVE_INFINITY) {
            event.setAmount(0.0F);
        }
    }
    
    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public static void onLivingDeathHighest(LivingDeathEvent event) {
        if(event.getEntityLiving() == null) return;
        if(event.getEntityLiving().world.isRemote) return;
        
        validateHealth(event.getEntityLiving());
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void onLivingDeathLowest(LivingDeathEvent event) {
        if(event.getEntityLiving() == null) return;
        if(event.getEntityLiving().world.isRemote) return;
        
        validateHealth(event.getEntityLiving());
    }

    private static void validateHealth(EntityLivingBase entity) {
        float health = entity.getHealth();
        if(Float.isNaN(health) || Float.isInfinite(health) || health < 0.0F) {
            entity.setHealth(0.0F);
        }
    }
}