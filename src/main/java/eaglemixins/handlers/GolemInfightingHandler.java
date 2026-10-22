package eaglemixins.handlers;

import eaglemixins.config.ForgeConfigHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Stops Battle Towers golems from targeting each other when one hits another (e.g. with a stray fireball).
 * The golem sets its attack target and vanilla its revenge target when hurt, both fire this event.
 */
public class GolemInfightingHandler {
    private static final ResourceLocation GOLEM = new ResourceLocation("battletowers", "golem");

    @SubscribeEvent
    public static void onSetAttackTarget(LivingSetAttackTargetEvent event) {
        if (!ForgeConfigHandler.server.preventGolemInfighting) return;
        EntityLivingBase entity = event.getEntityLiving();
        if (entity.world.isRemote || !isGolem(event.getTarget()) || !isGolem(entity)) return;
        if (entity.getRevengeTarget() == event.getTarget()) entity.setRevengeTarget(null);
        if (entity instanceof EntityLiving && ((EntityLiving) entity).getAttackTarget() == event.getTarget()) {
            ((EntityLiving) entity).setAttackTarget(null);
        }
    }

    private static boolean isGolem(Entity entity) {
        return entity != null && GOLEM.equals(EntityList.getKey(entity));
    }
}
