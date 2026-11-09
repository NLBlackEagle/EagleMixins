package eaglemixins.entity.ai;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;

import java.util.Random;

/**
 * Leaps at the attack target from a distance. The leap is aimed with a small ballistic simulation
 * (vanilla gravity and air drag) so that a distance multiplier of 1 lands on the target's position
 * at the moment of the leap. Moving targets, the multiplier and inaccuracy are what make it miss.
 */
public class EntityAILeap extends EntityAIBase {
    private static final int RETRY_TICKS = 20;
    private static final int MAX_AIR_TICKS = 100;
    // EntityLivingBase#travel while airborne: motionY = (motionY - 0.08) * 0.98, horizontal motion *= 0.91
    private static final double GRAVITY = 0.08;
    private static final double VERTICAL_DRAG = 0.98;
    private static final double HORIZONTAL_DRAG = 0.91;

    private final EntityLiving mob;
    private final LeapSettings settings;
    private long nextAttemptTick;
    private int airTicks;

    public EntityAILeap(EntityLiving mob, LeapSettings settings) {
        this.mob = mob;
        this.settings = settings;
        this.setMutexBits(5); //same as vanilla leap: movement + jumping
    }

    @Override
    public boolean shouldExecute() {
        long now = mob.world.getTotalWorldTime();
        if (now < nextAttemptTick) return false;
        if (!mob.onGround || mob.isRiding() || mob.isInWater() || mob.isInLava()) return false;

        EntityLivingBase target = mob.getAttackTarget();
        if (target == null || !target.isEntityAlive()) return false;
        if (target.posY - mob.posY < settings.minHeightAbove) return false;

        double dx = target.posX - mob.posX;
        double dz = target.posZ - mob.posZ;
        double distSq = dx * dx + dz * dz;
        if (distSq < settings.minRange * settings.minRange || distSq > settings.maxRange * settings.maxRange) return false;
        if (settings.lineOfSight && !mob.getEntitySenses().canSee(target)) return false;

        if (mob.getRNG().nextFloat() >= settings.chance) {
            nextAttemptTick = now + RETRY_TICKS;
            return false;
        }
        return true;
    }

    @Override
    public void startExecuting() {
        EntityLivingBase target = mob.getAttackTarget();
        if (target == null) return;
        Random rand = mob.getRNG();

        double aimX = target.posX;
        double aimZ = target.posZ;
        double offset = settings.inaccuracy.roll(rand);
        if (offset > 0) {
            double angle = rand.nextDouble() * Math.PI * 2;
            aimX += Math.cos(angle) * offset;
            aimZ += Math.sin(angle) * offset;
        }

        double dx = aimX - mob.posX;
        double dz = aimZ - mob.posZ;
        double dist = Math.sqrt(dx * dx + dz * dz);
        double vy = settings.verticalPower.roll(rand);

        if (dist > 1.0E-4) {
            int flightTicks = flightTicks(vy, target.posY - mob.posY);
            // Total horizontal distance covered = speed * (1 + 0.91 + 0.91^2 + ... over flightTicks)
            double distPerSpeed = (1 - Math.pow(HORIZONTAL_DRAG, flightTicks)) / (1 - HORIZONTAL_DRAG);
            double speed = Math.min(dist * settings.distanceMultiplier.roll(rand) / distPerSpeed, settings.maxHorizontalSpeed);
            mob.motionX = dx / dist * speed;
            mob.motionZ = dz / dist * speed;
        }
        mob.motionY = vy;
        mob.isAirBorne = true;
        mob.faceEntity(target, 360.0F, 360.0F);

        airTicks = 0;
        nextAttemptTick = mob.world.getTotalWorldTime() + Math.max(0, Math.round(settings.cooldown.roll(rand)));
    }

    /** Ticks until a leap with upward speed vy comes back down to height h, or until its apex if h is out of reach. */
    private static int flightTicks(double vy, double h) {
        double y = 0;
        double v = vy;
        for (int tick = 1; tick <= MAX_AIR_TICKS; tick++) {
            y += v;
            v = (v - GRAVITY) * VERTICAL_DRAG;
            if (v < 0 && y <= h) return tick;
        }
        return MAX_AIR_TICKS;
    }

    @Override
    public boolean shouldContinueExecuting() {
        // the mob is still on the ground on the first tick after the motion is applied
        return airTicks < MAX_AIR_TICKS && (airTicks < 2 || !mob.onGround);
    }

    @Override
    public void updateTask() {
        airTicks++;
        if (settings.preventFallDamage) mob.fallDistance = 0;
    }
}
