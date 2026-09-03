package eaglemixins.handlers;

import eaglemixins.compat.ModLoadedUtil;
import eaglemixins.compat.RLCombatUtil;
import eaglemixins.config.ForgeConfigHandler;
import eaglemixins.config.folders.WeaponDamageConfig;
import eaglemixins.util.IDamageSource_IsCritFlagMixin;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class WeaponDamageHandler {

    // Modified copy from So Many Enchantments
    public static boolean isDamageSourceAllowed(DamageSource source) {
        if(source == null) {
            return false;
        }
        if(!(source.getTrueSource() instanceof EntityLivingBase)) {
            return false;
        }
        if(source.getTrueSource() instanceof EntityPlayer && !(source.getImmediateSource() instanceof EntityPlayer) && source.getImmediateSource() instanceof EntityLivingBase) {
            return false;
        }
        return "player".equals(source.damageType) || "mob".equals(source.damageType);
    }

    // Handle Crit Damage Bonus like SME
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onCriticalHit(CriticalHitEvent event) {
        if(ModLoadedUtil.rlcombat.isLoaded() && !RLCombatUtil.isCriticalHitEventStrong(event)) return;
        EntityLivingBase attacker = event.getEntityLiving();
        if(attacker == null) return;
        if(!(event.getTarget() instanceof EntityLivingBase)) return;
        ItemStack stack = attacker.getHeldItemMainhand();
        if(ModLoadedUtil.rlcombat.isLoaded()) stack = RLCombatUtil.getCriticalHitEventStack(event, attacker);
        if(stack.isEmpty()) return;

        if(event.getResult() == Event.Result.DENY) return;
        if(event.getResult() == Event.Result.ALLOW || (event.isVanillaCritical() && event.getResult() == Event.Result.DEFAULT)) {
            WeaponDamageConfig.CritEntry entry = ForgeConfigHandler.weapondamage.getCritEntry(stack);
            if (entry != null && !entry.afterCrit) {
                event.setDamageModifier(event.getDamageModifier() * entry.multiplier);
            }
        }
    }

    // Handle Total Damage Multiplier Bonus like SME
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onLivingDamage(LivingDamageEvent event) {
        DamageSource damageSource = event.getSource();
        if(!isDamageSourceAllowed(damageSource)) return;
        if(event.getAmount() <= 1.0F) return;
        EntityLivingBase attacker = (EntityLivingBase)damageSource.getTrueSource();
        if(attacker == null) return;
        EntityLivingBase victim = event.getEntityLiving();
        if(victim == null) return;
        ItemStack stack = attacker.getHeldItemMainhand();
        if(stack.isEmpty()) return;

        if(damageSource instanceof IDamageSource_IsCritFlagMixin && ((IDamageSource_IsCritFlagMixin) damageSource).eagleMixins$isCrit()) {
            WeaponDamageConfig.CritEntry crit = ForgeConfigHandler.weapondamage.getCritEntry(stack);
            if (crit != null && crit.afterCrit) {
                event.setAmount(event.getAmount() * crit.multiplier);
            }
        }

        WeaponDamageConfig.RangeEntry range = ForgeConfigHandler.weapondamage.getRangeEntry(stack);
        if (range != null) {
            event.setAmount(event.getAmount() * range.compute(attacker.getDistance(victim)));
        }
    }
}
