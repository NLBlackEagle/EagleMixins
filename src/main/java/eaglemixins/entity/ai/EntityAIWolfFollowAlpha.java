package eaglemixins.entity.ai;

import eaglemixins.config.ForgeConfigHandler;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.passive.EntityWolf;

import javax.annotation.Nullable;

/** Pack members walk back to their alpha when they wander further away than "Pack Follow Distance" */
public class EntityAIWolfFollowAlpha extends EntityAIBase {
    private static final int REPATH_INTERVAL = 10;

    private final EntityWolf wolf;
    private final EntityAIWolfPack pack;
    @Nullable private EntityWolf alpha = null;
    private int repathCooldown = 0;

    public EntityAIWolfFollowAlpha(EntityWolf wolf, EntityAIWolfPack pack) {
        this.wolf = wolf;
        this.pack = pack;
        this.setMutexBits(1); // move
    }

    @Override
    public boolean shouldExecute() {
        if (!ForgeConfigHandler.wolfpack.enabled || wolf.isTamed() || wolf.isChild() || wolf.isSitting() || wolf.getAttackTarget() != null) return false;
        alpha = pack.getPackAlpha();
        if (alpha == null) return false;
        double follow = ForgeConfigHandler.wolfpack.packFollowDistance;
        return wolf.getDistanceSq(alpha) > follow * follow;
    }

    @Override
    public boolean shouldContinueExecuting() {
        if (alpha == null || !alpha.isEntityAlive() || wolf.getAttackTarget() != null || pack.getPackAlpha() != alpha) return false;
        double stop = ForgeConfigHandler.wolfpack.packFollowDistance * 0.5D;
        return wolf.getDistanceSq(alpha) > stop * stop;
    }

    @Override
    public void startExecuting() {
        repathCooldown = 0;
    }

    @Override
    public void resetTask() {
        alpha = null;
        wolf.getNavigator().clearPath();
    }

    @Override
    public void updateTask() {
        if (alpha == null || --repathCooldown > 0) return;
        repathCooldown = REPATH_INTERVAL;
        wolf.getNavigator().tryMoveToEntityLiving(alpha, 1.0D);
    }
}
