package eaglemixins.entity.ai;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;

/** An alpha that lost a duel against another alpha runs away from the winner for a while (see {@link EntityAIWolfPack#isFleeing}) */
public class EntityAIWolfFlee extends EntityAIBase {
    private static final double FLEE_SPEED = 1.4D;

    private final EntityWolf wolf;
    private final EntityAIWolfPack pack;

    public EntityAIWolfFlee(EntityWolf wolf, EntityAIWolfPack pack) {
        this.wolf = wolf;
        this.pack = pack;
        this.setMutexBits(3); // move + look
    }

    @Override
    public boolean shouldExecute() {
        EntityWolf from = pack.getFleeFrom();
        if (from == null) return false;
        Vec3d away = RandomPositionGenerator.findRandomTargetBlockAwayFrom(wolf, 16, 7, new Vec3d(from.posX, from.posY, from.posZ));
        return away != null && wolf.getNavigator().tryMoveToXYZ(away.x, away.y, away.z, FLEE_SPEED);
    }

    @Override
    public void startExecuting() {
        @Nullable EntityWolf from = pack.getFleeFrom();
        if (from != null && wolf.getAttackTarget() == from) wolf.setAttackTarget(null);
    }

    @Override
    public boolean shouldContinueExecuting() {
        return pack.isFleeing() && !wolf.getNavigator().noPath();
    }

    @Override
    public void resetTask() {
        wolf.getNavigator().clearPath();
    }
}
