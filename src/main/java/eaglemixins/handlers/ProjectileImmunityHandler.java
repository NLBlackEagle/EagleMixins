package eaglemixins.handlers;

import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ProjectileImmunityHandler {

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {

        if (event.getEntityLiving() == null) return;
        if (event.getEntityLiving().getTags().isEmpty()) return;
        if (!(event.getSource().damageType.equals("arrow"))) return;
        if (!(event.getEntityLiving().getTags().contains("projectileimmunity"))) return;

        event.setCanceled(true);
    }
}
