package eaglemixins.handlers;

import eaglemixins.config.ForgeConfigHandler;
import eaglemixins.config.folders.DragonfireConfig;
import eaglemixins.potion.PotionDragonfire;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class DragonfireHandler {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingDamage(LivingDamageEvent event) {
        if (event.getAmount() <= 0) return;
        EntityLivingBase victim = event.getEntityLiving();
        if (victim == null || victim.world.isRemote) return;

        DamageSource source = event.getSource();
        if (!WeaponDamageHandler.isDamageSourceAllowed(source)) return;
        if (source.getImmediateSource() != source.getTrueSource()) return; // melee only

        EntityLivingBase attacker = (EntityLivingBase) source.getTrueSource();
        ItemStack stack = attacker.getHeldItemMainhand();
        if (stack.isEmpty()) return;

        DragonfireConfig.DragonfireEntry entry = ForgeConfigHandler.dragonfire.weapons.get(stack.getItem().getRegistryName());
        if (entry == null) return;
        if (entry.chance < 1.0F && attacker.getRNG().nextFloat() >= entry.chance) return;

        victim.addPotionEffect(PotionDragonfire.create(entry.level));
        if (!victim.isImmuneToFire()) victim.setFire(PotionDragonfire.SECONDS_PER_LEVEL * Math.max(1, entry.level));
    }
}
