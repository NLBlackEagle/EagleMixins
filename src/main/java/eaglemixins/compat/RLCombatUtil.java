package eaglemixins.compat;

import bettercombat.mod.compat.EnchantCompatHandler;
import bettercombat.mod.event.RLCombatCriticalHitEvent;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.CriticalHitEvent;

public class RLCombatUtil {

    // This is a copy from So Many Enchantments for consistent effects

    public static ItemStack getArthropodStack(EntityLivingBase entity) {
        if(EnchantCompatHandler.arthropodFromOffhand) return entity.getHeldItemOffhand();
        return entity.getHeldItemMainhand();
    }

    public static ItemStack getKnockbackStack(EntityLivingBase entity) {
        if(EnchantCompatHandler.knockbackFromOffhand) return entity.getHeldItemOffhand();
        return entity.getHeldItemMainhand();
    }

    public static ItemStack getFireAspectStack(EntityLivingBase entity) {
        if(EnchantCompatHandler.fireAspectFromOffhand) return entity.getHeldItemOffhand();
        return entity.getHeldItemMainhand();
    }

    public static float getOnEntityDamagedAltStrength() {
        return EnchantCompatHandler.arthropodCooledStrength;
    }

    public static boolean isOnEntityDamagedAltStrong() {
        return getOnEntityDamagedAltStrength() > 0.9F;
    }

    public static float getAttackEntityFromStrength() {
        return EnchantCompatHandler.attackEntityFromCooledStrength;
    }

    public static boolean isAttackEntityFromStrong() {
        return getAttackEntityFromStrength() > 0.9F;
    }

    public static float getCriticalHitEventStrength(CriticalHitEvent event) {
        if(!(event instanceof RLCombatCriticalHitEvent)) return 1.0F;
        RLCombatCriticalHitEvent rlEvent = (RLCombatCriticalHitEvent)event;
        return rlEvent.getCooledStrength();
    }

    public static boolean isCriticalHitEventStrong(CriticalHitEvent event) {
        return getCriticalHitEventStrength(event) > 0.9F;
    }

    public static ItemStack getCriticalHitEventStack(CriticalHitEvent event, EntityLivingBase entity) {
        if(!(event instanceof RLCombatCriticalHitEvent)) return entity.getHeldItemMainhand();
        RLCombatCriticalHitEvent rlEvent = (RLCombatCriticalHitEvent)event;
        return rlEvent.getOffhand() ? entity.getHeldItemOffhand() : entity.getHeldItemMainhand();
    }
}
