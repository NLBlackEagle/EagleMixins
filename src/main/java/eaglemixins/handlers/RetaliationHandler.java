package eaglemixins.handlers;

import eaglemixins.config.ForgeConfigHandler;
import eaglemixins.config.folders.RetaliationConfig;
import eaglemixins.entity.ai.EntityAIPanicUnlessFighting;
import eaglemixins.entity.ai.EntityAIRetaliateMelee;
import eaglemixins.entity.ai.EntityAIRetaliateTarget;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.passive.AbstractHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Makes the mobs of the "Retaliation" config fight back.
 * Vanilla style mobs (EntityCreature) get a hurt-by-target AI that calls the herd for help, a melee attack if they have none
 * and their panic AI is skipped while they have someone to fight.
 * When a listed mob dies, nearby mobs of the same kind get the killer as revenge target, so one-hit kills still get a reaction.
 * That also covers Lycanites mobs (no EntityCreature, own AI): their revenge goal picks the revenge target up.
 */
public class RetaliationHandler {

    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (event.getWorld().isRemote) return;
        RetaliationConfig cfg = ForgeConfigHandler.retaliation;
        if (!cfg.enabled) return;
        Entity entity = event.getEntity();
        if (!(entity instanceof EntityCreature)) return;
        EntityCreature creature = (EntityCreature) entity;

        RetaliationConfig.Settings settings = cfg.get(creature);
        if (settings == null) return;

        if (!hasTask(creature.targetTasks, EntityAIHurtByTarget.class)) {
            creature.targetTasks.addTask(1, new EntityAIRetaliateTarget(creature));
        }

        if (!hasMeleeAttack(creature)) {
            creature.tasks.addTask(cfg.attackPriority, new EntityAIRetaliateMelee(creature, settings.speed, settings.damage, cfg.attackCooldown));
            wrapPanic(creature);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.isCanceled()) return;
        EntityLivingBase victim = event.getEntityLiving();
        if (victim.world.isRemote) return;
        RetaliationConfig cfg = ForgeConfigHandler.retaliation;
        if (!cfg.enabled || cfg.killAlertRadius <= 0) return;

        ResourceLocation victimId = EntityList.getKey(victim);
        if (victimId == null || cfg.get(victimId) == null) return;

        Entity source = event.getSource().getTrueSource();
        if (!(source instanceof EntityLivingBase) || source == victim || !source.isEntityAlive()) return;
        EntityLivingBase killer = (EntityLivingBase) source;
        if (killer instanceof EntityPlayer && (((EntityPlayer) killer).isCreative() || ((EntityPlayer) killer).isSpectator())) return;
        if (victimId.equals(EntityList.getKey(killer))) return; //infighting between the same kind

        List<EntityLiving> nearby = victim.world.getEntitiesWithinAABB(EntityLiving.class, victim.getEntityBoundingBox().grow(cfg.killAlertRadius), mob -> {
            if (mob == null || mob == victim || mob == killer || !mob.isEntityAlive()) return false;
            ResourceLocation id = EntityList.getKey(mob);
            if (id == null) return false;
            return cfg.killAlertsAnyListed ? cfg.get(id) != null : id.equals(victimId);
        });

        double radiusSq = cfg.killAlertRadius * cfg.killAlertRadius;
        for (EntityLiving mob : nearby) {
            if (mob.getDistanceSq(victim) > radiusSq || !canFight(mob)) continue;
            if (Objects.equals(mob.getAttackTarget(), killer)) continue;
            mob.setRevengeTarget(killer);
        }
    }

    /**
     * Lycanites (see RevengeGoalMixin): listed mobs fight back against survival players even when their creature group is
     * "wary". Against anything else the group config still decides, so animals keep running from predators.
     */
    public static boolean shouldFightPlayer(EntityLiving mob, Entity attacker) {
        if (!(attacker instanceof EntityPlayer)) return false;
        EntityPlayer player = (EntityPlayer) attacker;
        if (player.isCreative() || player.isSpectator()) return false;
        RetaliationConfig cfg = ForgeConfigHandler.retaliation;
        return cfg.enabled && cfg.get(mob) != null && canFight(mob);
    }

    /** Tamed mobs don't retaliate, babies only when "Babies Fight Back" is on */
    public static boolean canFight(EntityLiving mob) {
        if (isTamed(mob)) return false;
        return ForgeConfigHandler.retaliation.babiesFightBack || !mob.isChild();
    }

    public static boolean isTamed(EntityLiving mob) {
        if (mob instanceof IEntityOwnable && ((IEntityOwnable) mob).getOwnerId() != null) return true;
        return mob instanceof AbstractHorse && ((AbstractHorse) mob).isTame();
    }

    private static boolean hasTask(EntityAITasks tasks, Class<? extends EntityAIBase> type) {
        for (EntityAITasks.EntityAITaskEntry entry : tasks.taskEntries) {
            if (type.isInstance(entry.action)) return true;
        }
        return false;
    }

    /** Vanilla melee (and subclasses like ours), or a mod's own copy of it such as Epic Siege Mod's ESM_EntityAIAttackMelee */
    private static boolean hasMeleeAttack(EntityCreature creature) {
        for (EntityAITasks.EntityAITaskEntry entry : creature.tasks.taskEntries) {
            if (entry.action instanceof EntityAIAttackMelee) return true;
            if (entry.action.getClass().getSimpleName().contains("AttackMelee")) return true;
        }
        return false;
    }

    private static void wrapPanic(EntityCreature creature) {
        List<EntityAITasks.EntityAITaskEntry> panics = new ArrayList<>();
        for (EntityAITasks.EntityAITaskEntry entry : creature.tasks.taskEntries) {
            if (entry.action instanceof EntityAIPanic) panics.add(entry);
        }
        for (EntityAITasks.EntityAITaskEntry entry : panics) {
            creature.tasks.removeTask(entry.action);
            creature.tasks.addTask(entry.priority, new EntityAIPanicUnlessFighting(creature, entry.action));
        }
    }
}
