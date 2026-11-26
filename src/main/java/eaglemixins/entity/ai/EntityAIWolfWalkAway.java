package eaglemixins.entity.ai;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/**
 * A satisfied alpha (see {@link EntityAIWolfPack#isSatisfied}) walks off in a random direction for a while, its pack
 * follows it. Unlike fleeing it just walks, and stops to fight back when attacked.
 * Given a taming item meanwhile, it turns to the player and sits waiting for the next one ({@link EntityAIWolfPack#getWaitTarget})
 */
public class EntityAIWolfWalkAway extends EntityAIBase {
    private static final int REPATH_INTERVAL = 40;
    private static final double WALK_SPEED = 1.0D;
    /** How far ahead (blocks) along its direction it picks the next spot */
    private static final double LOOK_AHEAD = 12.0D;

    private final EntityWolf wolf;
    private final EntityAIWolfPack pack;
    private int repathCooldown = 0;

    public EntityAIWolfWalkAway(EntityWolf wolf, EntityAIWolfPack pack) {
        this.wolf = wolf;
        this.pack = pack;
        this.setMutexBits(3); // move + look
    }

    @Override
    public boolean shouldExecute() {
        return !wolf.isTamed() && wolf.getAttackTarget() == null && pack.isSatisfied();
    }

    @Override
    public void startExecuting() {
        repathCooldown = 0;
    }

    @Override
    public void resetTask() {
        wolf.getNavigator().clearPath();
        wolf.setSitting(false);
        wolf.setBegging(false);
    }

    @Override
    public void updateTask() {
        EntityPlayer waitFor = pack.getWaitTarget();
        if (waitFor != null) {
            wolf.getNavigator().clearPath();
            float yaw = (float) (MathHelper.atan2(waitFor.posZ - wolf.posZ, waitFor.posX - wolf.posX) * (180.0D / Math.PI)) - 90.0F;
            wolf.rotationYaw = yaw;
            wolf.renderYawOffset = yaw;
            wolf.getLookHelper().setLookPositionWithEntity(waitFor, 30.0F, (float) wolf.getVerticalFaceSpeed());
            if (!wolf.isSitting()) wolf.setSitting(true);
            wolf.setBegging(true);
            repathCooldown = 0;
            return;
        }
        if (wolf.isSitting()) {
            wolf.setSitting(false);
            wolf.setBegging(false);
        }

        if (--repathCooldown > 0 && !wolf.getNavigator().noPath()) return;
        repathCooldown = REPATH_INTERVAL;
        double angle = pack.getWalkAwayAngle();
        Vec3d towards = new Vec3d(wolf.posX + Math.cos(angle) * LOOK_AHEAD, wolf.posY, wolf.posZ + Math.sin(angle) * LOOK_AHEAD);
        Vec3d pos = RandomPositionGenerator.findRandomTargetBlockTowards(wolf, 16, 7, towards);
        if (pos != null) wolf.getNavigator().tryMoveToXYZ(pos.x, pos.y, pos.z, WALK_SPEED);
    }
}
