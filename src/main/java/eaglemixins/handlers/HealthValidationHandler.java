package eaglemixins.handlers;

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.api.damagesystem.AbstractDamageablePart;
import ichttt.mods.firstaid.api.event.FirstAidLivingDamageEvent;
import ichttt.mods.firstaid.common.network.MessageUpdatePart;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class HealthValidationHandler {

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onFirstAidLivingDamageLow(FirstAidLivingDamageEvent event) {
        if(event.getEntityPlayer()==null || event.getEntityPlayer().world.isRemote) return;
        for(AbstractDamageablePart part : event.getAfterDamage()) {
            if(Float.isNaN(part.currentHealth)) {
                part.currentHealth = 0.0F;
                part.heal(1.0F, null, false);
                if(event.getEntityPlayer() instanceof EntityPlayerMP) FirstAid.NETWORKING.sendTo(new MessageUpdatePart(part), (EntityPlayerMP) event.getEntityPlayer());
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onLivingHurt(LivingHurtEvent event) {
        validateHealth(event);
        if (Float.isNaN(event.getAmount())) {
            event.setAmount(1.0F);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onLivingDamage(LivingDamageEvent event) {
        validateHealth(event);
        if (!Float.isFinite(event.getAmount())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onLivingHeal(LivingHealEvent event) {
        validateHealth(event);
        if (!Float.isFinite(event.getAmount())) {
            event.setCanceled(true);
        }
    }

    private static void validateHealth(LivingEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        if (entity.world.isRemote) {
            return;
        }
        if (!Float.isFinite(entity.getHealth())) {
            entity.setHealth(1.0F);

            if (entity instanceof EntityPlayerMP) {
                ((EntityPlayerMP) entity).setPlayerHealthUpdated();
            }
        }
        if (!Float.isFinite(entity.getAbsorptionAmount())) {
            entity.setAbsorptionAmount(0.0F);
        }
    }
}
