package eaglemixins.handlers;

import eaglemixins.config.folders.PlayerBossConfig;
import eaglemixins.entity.EntityPlayerBoss;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Caps the damage of a single player hit on a player boss (Player Damage Cap). */
public class PlayerBossDamageHandler {

    // LivingDamageEvent is the final damage after armor; LOWEST so other mods' changes are included
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingDamage(LivingDamageEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayerBoss)) return;
        if (!(event.getSource().getTrueSource() instanceof EntityPlayer)) return;
        PlayerBossConfig.BossEntry entry = ((EntityPlayerBoss) event.getEntityLiving()).getEntry();
        if (entry == null || entry.playerDamageCap <= 0) return;
        if (event.getAmount() > entry.playerDamageCap) event.setAmount((float) entry.playerDamageCap);
    }
}
