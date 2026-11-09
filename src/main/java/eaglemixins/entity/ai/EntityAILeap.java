package eaglemixins.entity.ai;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;

import java.util.Random;

/**
 * Leaps at the attack target from a distance.
 * <p>
 * arc=true (default): the mob is steered along a smooth, symmetric arch from its position to the landing spot. Every tick the motion
 * is set so it reaches the next point of the arch, which also corrects small deviations. Hitting a wall or ceiling, or getting
 * knocked too far off the arch, stops the steering and the mob falls normally.
 * <p>
 * arc=false: a single push aimed with a small ballistic simulation (vanilla gravity and air drag). Vanilla air drag slows the horizontal
 * motion a lot faster than the vertical one, so this flies flat at first and drops down steeply at the end.
 * <p>
 * The landing spot is the target's position at the moment of the leap (times "distance"), or "leapDistance" blocks towards it when set.
 * Moving targets, the multiplier and inaccuracy are what make it miss.
 */
public class EntityAILeap extends EntityAIBase {
    private static final int RETRY_TICKS = 20;
    private static final int MAX_AIR_TICKS = 100;
    // EntityLivingBase#travel while airborne: motionY = (motionY - 0.08) * 0.98, horizontal motion *= 0.91
    private static final double GRAVITY = 0.08;
    private static final double VERTICAL_DRAG = 0.98;
    private static final double HORIZONTAL_DRAG = 0.91;
    // Arc steering
    private static final int MIN_ARC_TICKS = 6;
    private static final double MIN_ARC_HEIGHT = 1.25;
    private static final double MAX_ARC_DEVIATION = 1.5;

    private final EntityLiving mob;
    private final LeapSettings settings;
    private long nextAttemptTick;
    private int airTicks;

    private boolean steering;
    private double startX, startY, startZ;
    private double endX, endY, endZ;
    private double arcHeight;
    private int arcTicks;

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
        // Horizontal length of the leap: fixed leapDistance if set, otherwise the distance to the aim spot times the multiplier
        double fixed = settings.leapDistance.roll(rand);
        double leapLength = fixed > 0 ? fixed : dist * settings.distanceMultiplier.roll(rand);
        double dirX = dist > 1.0E-4 ? dx / dist : 0;
        double dirZ = dist > 1.0E-4 ? dz / dist : 0;
        double heightDiff = target.posY - mob.posY;

        airTicks = 0;
        steering = false;
        mob.getNavigator().clearPath();

        if (settings.arc && leapLength > 1.0E-4) {
            startX = mob.posX;
            startY = mob.posY;
            startZ = mob.posZ;
            endX = startX + dirX * leapLength;
            endY = startY + heightDiff;
            endZ = startZ + dirZ * leapLength;
            // Bump on top of the straight line between start and end, high enough to clear the higher end
            arcHeight = Math.max(Math.max(MIN_ARC_HEIGHT, leapLength * settings.arcHeight.roll(rand)), 1.0 + Math.abs(heightDiff) * 0.5);
            arcTicks = Math.max(MIN_ARC_TICKS, (int) Math.ceil(leapLength / Math.max(0.05, settings.arcSpeed.roll(rand))));
            steering = true;
        } else {
            double vy = settings.verticalPower.roll(rand);
            if (leapLength > 1.0E-4) {
                int flightTicks = flightTicks(vy, heightDiff);
                // Total horizontal distance covered = speed * (1 + 0.91 + 0.91^2 + ... over flightTicks)
                double distPerSpeed = (1 - Math.pow(HORIZONTAL_DRAG, flightTicks)) / (1 - HORIZONTAL_DRAG);
                double speed = Math.min(leapLength / distPerSpeed, settings.maxHorizontalSpeed);
                mob.motionX = dirX * speed;
                mob.motionZ = dirZ * speed;
            }
            mob.motionY = vy;
        }
        mob.isAirBorne = true;
        mob.faceEntity(target, 360.0F, 360.0F);

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
        if (steering) steer();
    }

    /** Sets the motion so the mob moves to the point of the arch for this tick (the AI runs right before the mob moves). */
    private void steer() {
        if (airTicks > 1 && (mob.collidedHorizontally || (mob.collidedVertically && !mob.onGround) || mob.isInWater() || mob.isInLava())) {
            stopSteering();
            return;
        }

        double now = (airTicks - 1) / (double) arcTicks;
        if (airTicks > 1 && distanceSqToArc(now) > MAX_ARC_DEVIATION * MAX_ARC_DEVIATION) {
            stopSteering();
            return;
        }

        if (airTicks > arcTicks) {
            // Reached the landing spot, keep the end velocity of the arch and let gravity take over from here (e.g. landing lower)
            double s = 1.0, ds = 1.0 / arcTicks;
            mob.motionX = arcX(s) - arcX(s - ds);
            mob.motionY = arcY(s) - arcY(s - ds);
            mob.motionZ = arcZ(s) - arcZ(s - ds);
            steering = false;
            return;
        }

        double next = airTicks / (double) arcTicks;
        mob.motionX = arcX(next) - mob.posX;
        mob.motionY = arcY(next) - mob.posY;
        mob.motionZ = arcZ(next) - mob.posZ;
    }

    private void stopSteering() {
        steering = false;
        // Keep a bit of forward momentum, gravity handles the rest
        mob.motionX *= 0.5;
        mob.motionZ *= 0.5;
        if (mob.motionY > 0) mob.motionY = 0;
    }

    private double distanceSqToArc(double s) {
        double dx = arcX(s) - mob.posX, dy = arcY(s) - mob.posY, dz = arcZ(s) - mob.posZ;
        return dx * dx + dy * dy + dz * dz;
    }

    private double arcX(double s) {
        return startX + (endX - startX) * s;
    }

    private double arcZ(double s) {
        return startZ + (endZ - startZ) * s;
    }

    /** Straight line from start to end height plus a symmetric bump that is arcHeight high in the middle. */
    private double arcY(double s) {
        return startY + (endY - startY) * s + 4.0 * arcHeight * s * (1.0 - s);
    }
}
