package eaglemixins.entity.ai;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.WorldServer;

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
    private static final double MAX_RISE_SPEED = 0.6; // blocks per tick at take-off, a normal jump is 0.42
    private static final double MAX_ARC_DEVIATION = 1.5;
    // Homerun arch: how far a knockback of strength 1 throws a mob from the ground (vanilla: speed = strength, air drag 0.91,
    // about 10 ticks in the air), times homerunPower. Height and take-off speed are capped so it stays a hit, not a launch
    private static final double BLOCKS_PER_STRENGTH = 6.5;
    private static final double MAX_HOMERUN_HEIGHT = 8.0;
    private static final double MAX_HOMERUN_RISE_SPEED = 1.2;

    private final EntityLiving mob;
    private final LeapSettings settings;
    private long nextAttemptTick;
    private int airTicks;

    private boolean steering;
    private double startX, startY, startZ;
    private double endX, endY, endZ;
    private double arcHeight;
    private int arcTicks;
    private int arcTick; // ticks since the current arch (leap or homerun) started

    // Homerun: knocked out of the air while steering
    private long homerunTick = -1;
    private float homerunStrength;
    private boolean homerunPending; // the homerun arch starts on the mob's next tick, when the whole hit is added up
    private double homerunDirX, homerunDirZ;
    private float clangPower = -1; // >= 0: anvil clang still to play for the last homerun
    private float trailStrength = 0; // > 0: homerun past the loudest clang, leaves a trail until it lands

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
        double heightDiff = target.posY - mob.posY;
        // Too far to reach in one leap: leap as far as allowed towards the target, it still closes the gap faster than walking
        if (settings.maxLeapLength > 0 && leapLength > settings.maxLeapLength) {
            if (dist > 1.0E-4) heightDiff *= Math.min(1.0, settings.maxLeapLength / dist);
            leapLength = settings.maxLeapLength;
        }
        double dirX = dist > 1.0E-4 ? dx / dist : 0;
        double dirZ = dist > 1.0E-4 ? dz / dist : 0;

        airTicks = 0;
        arcTick = 0;
        steering = false;
        homerunPending = false;
        mob.getNavigator().clearPath();

        if (settings.arc && leapLength > 1.0E-4) {
            startX = mob.posX;
            startY = mob.posY;
            startZ = mob.posZ;
            endX = startX + dirX * leapLength;
            endY = startY + heightDiff;
            endZ = startZ + dirZ * leapLength;
            // Bump on top of the straight line between start and end. The minimum grows with the leap, a fixed one made short hops
            // far too high: 0.65 for 2 blocks, 0.8 for 3, the full MIN_ARC_HEIGHT from 6. Higher targets need some extra clearance.
            double minHeight = Math.min(MIN_ARC_HEIGHT, 0.35 + 0.15 * leapLength);
            arcHeight = Math.max(Math.max(minHeight, leapLength * settings.arcHeight.roll(rand)), heightDiff > 0 ? 0.5 + heightDiff * 0.5 : 0);
            // Short hops fly a bit slower (70% at 2 blocks, full speed from 5), and the rise is capped at a strong jump,
            // taking a few ticks longer instead of shooting up
            double speed = Math.max(0.05, settings.arcSpeed.roll(rand)) * Math.min(1.0, 0.5 + leapLength / 10.0);
            int ticksForLength = (int) Math.ceil(leapLength / speed);
            int ticksForRise = (int) Math.ceil(4.0 * arcHeight / MAX_RISE_SPEED); // the arch starts rising at 4 * height / ticks
            arcTicks = Math.max(MIN_ARC_TICKS, Math.max(ticksForLength, ticksForRise));
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

    /** Landing early (on a ledge mid-arch) or being interrupted ends the leap too, after that hits are normal knockback again */
    @Override
    public void resetTask() {
        steering = false;
        homerunPending = false;
        playClang();
        trailStrength = 0;
    }

    /** Puffs and sparks behind a mob that was batted away harder than the loudest clang */
    private void spawnTrail() {
        if (trailStrength <= 0 || !(mob.world instanceof WorldServer)) return;
        if (airTicks > 2 && mob.onGround) {
            trailStrength = 0;
            return;
        }
        WorldServer world = (WorldServer) mob.world;
        double x = mob.posX, y = mob.posY + mob.height * 0.5, z = mob.posZ;
        double spread = mob.width * 0.3;
        world.spawnParticle(EnumParticleTypes.CLOUD, x, y, z, 1 + Math.round(2 * trailStrength), spread, spread, spread, 0.01);
        world.spawnParticle(EnumParticleTypes.CRIT, x, y, z, 2 + Math.round(3 * trailStrength), spread, spread, spread, 0.15);
    }

    private void playClang() {
        if (clangPower < 0) return;
        float power = clangPower;
        clangPower = -1;
        mob.world.playSound(null, mob.posX, mob.posY, mob.posZ, SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.PLAYERS,
                0.5F + 0.5F * power, 1.1F + 0.6F * power + mob.getRNG().nextFloat() * 0.1F);
    }

    @Override
    public void updateTask() {
        playClang();
        if (homerunPending) startHomerunArc();
        airTicks++;
        if (steering) arcTick++;
        spawnTrail();
        if (settings.preventFallDamage) mob.fallDistance = 0;
        if (steering) steer();
    }

    /** Sets the motion so the mob moves to the point of the arch for this tick (the AI runs right before the mob moves). */
    private void steer() {
        if (arcTick > 1 && (mob.collidedHorizontally || (mob.collidedVertically && !mob.onGround) || mob.isInWater() || mob.isInLava())) {
            stopSteering();
            return;
        }

        double now = (arcTick - 1) / (double) arcTicks;
        if (arcTick > 1 && distanceSqToArc(now) > MAX_ARC_DEVIATION * MAX_ARC_DEVIATION) {
            stopSteering();
            return;
        }

        if (arcTick > arcTicks) {
            // Reached the landing spot, keep the end velocity of the arch and let gravity take over from here (e.g. landing lower)
            double s = 1.0, ds = 1.0 / arcTicks;
            mob.motionX = arcX(s) - arcX(s - ds);
            mob.motionY = arcY(s) - arcY(s - ds);
            mob.motionZ = arcZ(s) - arcZ(s - ds);
            steering = false;
            return;
        }

        double next = arcTick / (double) arcTicks;
        mob.motionX = arcX(next) - mob.posX;
        mob.motionY = arcY(next) - mob.posY;
        mob.motionZ = arcZ(next) - mob.posZ;
    }

    // Knockback strengths: plain hit 0.4, sprint + Knockback II 1.9 (loudest clang), SME Advanced Knockback II 2.9 (full trail)
    private static final float CLANG_FULL_STRENGTH = 1.9F;
    private static final float TRAIL_FULL_STRENGTH = 2.9F;

    /**
     * Knockback while flying an arch (the leap, or a homerun arch). The steering would overwrite vanilla knockback on the
     * next tick, so instead the mob gets batted away from the attacker on an arch of its own (startHomerunArc).
     * A player hit fires two knockbacks in the same tick (base 0.4, then 0.5 per Knockback level / sprinting), they are
     * added up, the arch starts on the mob's next tick when the whole hit is known.
     * @return true if handled, the vanilla knockback should then be canceled
     */
    public boolean onKnockback(float strength, double ratioX, double ratioZ) {
        if (!settings.homerun) return false;
        long now = mob.world.getTotalWorldTime();
        boolean sameHit = homerunTick == now;
        if (!steering && !sameHit) return false;

        if (!sameHit) {
            // ratio points from the mob towards the attacker, the mob goes the other way
            double len = Math.sqrt(ratioX * ratioX + ratioZ * ratioZ);
            if (len > 1.0E-4) {
                homerunDirX = -ratioX / len;
                homerunDirZ = -ratioZ / len;
            } else {
                // no direction given: straight back along the arch it was flying
                double lx = startX - endX, lz = startZ - endZ, l = Math.sqrt(lx * lx + lz * lz);
                homerunDirX = l > 1.0E-4 ? lx / l : 0;
                homerunDirZ = l > 1.0E-4 ? lz / l : 0;
            }
            homerunStrength = 0;
        }
        homerunStrength += strength;
        homerunTick = now;
        steering = false;
        homerunPending = true;

        // The clang waits for the mob's own tick too
        clangPower = Math.min(1.0F, homerunStrength / CLANG_FULL_STRENGTH);
        // Past the loudest clang the mob leaves a trail, denser the stronger the hit
        trailStrength = homerunStrength > CLANG_FULL_STRENGTH
                ? Math.min(1.0F, (homerunStrength - CLANG_FULL_STRENGTH) / (TRAIL_FULL_STRENGTH - CLANG_FULL_STRENGTH)) : 0;
        return true;
    }

    /**
     * The homerun: an arch away from the attacker, homerunPower times as far as the same knockback throws a mob from the
     * ground (doubled by default), up to homerunMax. It climbs steeply and flattens out at the far end, from there it keeps
     * flying and gravity brings it down. Knockback resistance shortens it the same way it weakens normal knockback.
     */
    private void startHomerunArc() {
        homerunPending = false;
        Random rand = mob.getRNG();
        double resistance = MathHelper.clamp(mob.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).getAttributeValue(), 0, 1);
        double length = Math.min(settings.homerunMax, homerunStrength * BLOCKS_PER_STRENGTH * settings.homerunPower) * (1 - resistance);
        if (length < 0.5) {
            // Barely moved: just drops out of the air
            mob.motionX = 0;
            mob.motionZ = 0;
            if (mob.motionY > 0) mob.motionY = 0;
            return;
        }

        startX = mob.posX;
        startY = mob.posY;
        startZ = mob.posZ;
        endX = startX + homerunDirX * length;
        endZ = startZ + homerunDirZ * length;
        double rise = MathHelper.clamp(length * settings.arcHeight.roll(rand), 0.5, MAX_HOMERUN_HEIGHT);
        endY = startY + rise;
        arcHeight = rise / 4.0; // with the rise to the end this makes the arch level out exactly at the far end

        // Faster for harder hits: plain hit ~0.6 blocks per tick, Advanced Knockback II ~1.4
        double speed = 0.5 + 0.3 * homerunStrength;
        int ticksForLength = (int) Math.ceil(length / speed);
        int ticksForRise = (int) Math.ceil(2.0 * rise / MAX_HOMERUN_RISE_SPEED); // takes off at 2 * rise / ticks
        arcTicks = Math.max(MIN_ARC_TICKS, Math.max(ticksForLength, ticksForRise));
        arcTick = 0;
        airTicks = 0;
        steering = true;
        mob.isAirBorne = true;
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
