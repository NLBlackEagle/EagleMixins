package eaglemixins.entity.ai;

import eaglemixins.config.folders.PlayerBossConfig;
import eaglemixins.entity.EntityPlayerBoss;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.math.Vec3d;

/** Target tasks for the player boss AIs that copy how a vanilla mob picks its targets */
public class BossTargetAI {

    /** Looks for a target on every AI update instead of vanilla's 1 in 10, bosses notice you right away */
    public static <T extends EntityLivingBase> EntityAINearestAttackableTarget<T> nearest(EntityCreature boss, Class<T> targetClass, boolean checkSight) {
        return new EntityAINearestAttackableTarget<>(boss, targetClass, 1, checkSight, false, null);
    }

    /** Fights back, and with callsHelp alerts nearby bosses with the same AI. Zombie pigman bosses get angry like zombie pigmen */
    public static class HurtByTarget extends EntityAIHurtByTarget {
        private final EntityPlayerBoss boss;

        public HurtByTarget(EntityPlayerBoss boss, boolean callsHelp) {
            super(boss, callsHelp);
            this.boss = boss;
        }

        @Override
        protected void setEntityAttackTarget(EntityCreature creature, EntityLivingBase target) {
            if (!(creature instanceof EntityPlayerBoss) || ((EntityPlayerBoss) creature).getAI() != boss.getAI()) return;
            if (boss.getAI() == PlayerBossConfig.BossAI.ZOMBIE_PIGMAN) ((EntityPlayerBoss) creature).becomeAngryAt(target);
            else ((EntityPlayerBoss) creature).respondTo(target);
        }
    }

    /** Spider: only looks for targets in the dark */
    public static class InDark<T extends EntityLivingBase> extends EntityAINearestAttackableTarget<T> {
        public InDark(EntityCreature boss, Class<T> targetClass) {
            super(boss, targetClass, 1, true, false, null);
        }

        @Override
        public boolean shouldExecute() {
            return taskOwner.getBrightness() < 0.5F && super.shouldExecute();
        }
    }

    /** Zombie pigman: only looks for players while angry */
    public static class WhileAngry extends EntityAINearestAttackableTarget<EntityPlayer> {
        private final EntityPlayerBoss boss;

        public WhileAngry(EntityPlayerBoss boss) {
            super(boss, EntityPlayer.class, 1, true, false, null);
            this.boss = boss;
        }

        @Override
        public boolean shouldExecute() {
            return boss.isAngry() && super.shouldExecute();
        }
    }

    /** Enderman: targets the nearest player looking straight at it, unless they wear a pumpkin */
    public static class Stare extends EntityAINearestAttackableTarget<EntityPlayer> {
        public Stare(EntityCreature boss) {
            super(boss, EntityPlayer.class, 0, false, false, null);
        }

        @Override
        public boolean shouldExecute() {
            double range = getTargetDistance();
            EntityPlayer nearest = null;
            double nearestDist = range * range;
            for (EntityPlayer player : taskOwner.world.playerEntities) {
                double dist = taskOwner.getDistanceSq(player);
                if (dist >= nearestDist || !isSuitableTarget(player, false) || !isStaring(player)) continue;
                nearest = player;
                nearestDist = dist;
            }
            targetEntity = nearest;
            return nearest != null;
        }

        // Same check as EntityEnderman.shouldAttackPlayer
        private boolean isStaring(EntityPlayer player) {
            if (player.inventory.armorInventory.get(3).getItem() == Item.getItemFromBlock(Blocks.PUMPKIN)) return false;
            Vec3d look = player.getLook(1.0F).normalize();
            Vec3d toBoss = new Vec3d(taskOwner.posX - player.posX,
                    taskOwner.getEntityBoundingBox().minY + taskOwner.getEyeHeight() - (player.posY + player.getEyeHeight()),
                    taskOwner.posZ - player.posZ);
            double dist = toBoss.length();
            return look.dotProduct(toBoss.normalize()) > 1.0D - 0.025D / dist && player.canEntityBeSeen(taskOwner);
        }
    }
}
