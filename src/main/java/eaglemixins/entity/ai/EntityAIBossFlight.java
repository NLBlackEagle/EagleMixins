package eaglemixins.entity.ai;

import eaglemixins.config.folders.PlayerBossConfig;
import eaglemixins.entity.EntityPlayerBoss;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.MathHelper;

/**
 * Flight for player bosses, replaces walking and the normal attack AI while active (same mutex bits).
 * Bow bosses hover above their target at a distance and keep shooting, melee bosses fly straight at it and land once
 * the target can be reached on foot again. Gravity is off while flying.
 */
public class EntityAIBossFlight extends EntityAIBase {
    private final EntityPlayerBoss boss;
    private int pathCheckCooldown;
    private boolean unreachable;
    private int attackCooldown;

    public EntityAIBossFlight(EntityPlayerBoss boss) {
        this.boss = boss;
        setMutexBits(3);
    }

    private PlayerBossConfig.FlightMode mode() {
        PlayerBossConfig.BossEntry entry = boss.getEntry();
        return entry == null ? PlayerBossConfig.FlightMode.NEVER : entry.flight.mode;
    }

    @Override
    public boolean shouldExecute() {
        PlayerBossConfig.FlightMode mode = mode();
        EntityLivingBase target = boss.getAttackTarget();
        if (mode == PlayerBossConfig.FlightMode.NEVER || target == null || !target.isEntityAlive()) return false;
        return mode == PlayerBossConfig.FlightMode.ALWAYS || isOutOfReach(target);
    }

    @Override
    public boolean shouldContinueExecuting() {
        PlayerBossConfig.FlightMode mode = mode();
        EntityLivingBase target = boss.getAttackTarget();
        if (mode == PlayerBossConfig.FlightMode.NEVER || target == null || !target.isEntityAlive()) return false;
        return mode == PlayerBossConfig.FlightMode.ALWAYS || isOutOfReach(target);
    }

    /** Target is much higher or lower, or there is no walking path to it (path checked once a second). */
    private boolean isOutOfReach(EntityLivingBase target) {
        if (Math.abs(target.posY - boss.posY) > 3.0D) return true;
        if (--pathCheckCooldown <= 0) {
            pathCheckCooldown = 20;
            unreachable = boss.getNavigator().getPathToEntityLiving(target) == null;
        }
        return unreachable;
    }

    @Override
    public void startExecuting() {
        boss.setNoGravity(true);
        boss.getNavigator().clearPath();
        attackCooldown = 10;
    }

    @Override
    public void resetTask() {
        boss.setNoGravity(false);
        boss.setSwingingArms(false);
        pathCheckCooldown = 0;
    }

    @Override
    public void updateTask() {
        PlayerBossConfig.BossEntry entry = boss.getEntry();
        EntityLivingBase target = boss.getAttackTarget();
        if (entry == null || target == null) return;
        PlayerBossConfig.Flight flight = entry.flight;
        boolean bow = boss.isHoldingBow();

        // Where to fly to
        double goalX, goalY, goalZ;
        if (bow) {
            double dx = boss.posX - target.posX, dz = boss.posZ - target.posZ;
            double len = Math.sqrt(dx * dx + dz * dz);
            if (len < 0.01D) { dx = 1; dz = 0; len = 1; }
            goalX = target.posX + dx / len * flight.keepDistance;
            goalY = target.posY + flight.hoverHeight;
            goalZ = target.posZ + dz / len * flight.keepDistance;
        } else {
            goalX = target.posX;
            goalY = target.posY + (target.height - boss.height) * 0.5D;
            goalZ = target.posZ;
        }

        // Steer towards it, capped at the configured speed
        double mx = goalX - boss.posX, my = goalY - boss.posY, mz = goalZ - boss.posZ;
        double dist = Math.sqrt(mx * mx + my * my + mz * mz);
        if (dist > 0.3D) {
            double accel = flight.speed * 0.2D;
            boss.motionX += mx / dist * accel;
            boss.motionY += my / dist * accel;
            boss.motionZ += mz / dist * accel;
        } else {
            boss.motionX *= 0.8D;
            boss.motionY *= 0.8D;
            boss.motionZ *= 0.8D;
        }
        double speed = Math.sqrt(boss.motionX * boss.motionX + boss.motionY * boss.motionY + boss.motionZ * boss.motionZ);
        if (speed > flight.speed) {
            double f = flight.speed / speed;
            boss.motionX *= f;
            boss.motionY *= f;
            boss.motionZ *= f;
        }

        // Face the target
        boss.getLookHelper().setLookPositionWithEntity(target, 30.0F, 30.0F);
        double tx = target.posX - boss.posX, tz = target.posZ - boss.posZ;
        boss.rotationYaw = (float) (MathHelper.atan2(tz, tx) * (180D / Math.PI)) - 90.0F;
        boss.renderYawOffset = boss.rotationYaw;

        // Attack while flying
        if (attackCooldown > 0) attackCooldown--;
        double distSq = boss.getDistanceSq(target);
        if (bow) {
            boss.setSwingingArms(true);
            if (attackCooldown <= 0 && distSq < 32 * 32 && boss.getEntitySenses().canSee(target)) {
                boss.attackEntityWithRangedAttack(target, 1.0F);
                attackCooldown = entry.bowInterval;
            }
        } else {
            if (attackCooldown <= 0 && distSq <= boss.getMeleeReachSqr(target)) {
                boss.swingArm(EnumHand.MAIN_HAND);
                boss.attackEntityAsMob(target);
                attackCooldown = 20;
            }
        }
    }
}
