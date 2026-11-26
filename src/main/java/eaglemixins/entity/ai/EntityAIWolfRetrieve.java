package eaglemixins.entity.ai;

import eaglemixins.config.ForgeConfigHandler;
import eaglemixins.handlers.WolfPackHandler;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;

import javax.annotation.Nullable;

/**
 * An alpha waiting for its tribute (see {@link EntityAIWolfPack#getRetrieveTarget}) walks up to the player, sits down
 * next to them and begs until it gets its calming items or loses its patience
 */
public class EntityAIWolfRetrieve extends EntityAIBase {
    private static final int REPATH_INTERVAL = 10;
    /** Stands up again once the player is this much further away than "Retrieval Sit Distance" */
    private static final double STAND_UP_MARGIN = 1.0D;

    private final EntityWolf wolf;
    private final EntityAIWolfPack pack;
    @Nullable private EntityPlayer target = null;
    private int repathCooldown = 0;

    public EntityAIWolfRetrieve(EntityWolf wolf, EntityAIWolfPack pack) {
        this.wolf = wolf;
        this.pack = pack;
        this.setMutexBits(3); // move + look
    }

    @Override
    public boolean shouldExecute() {
        target = pack.getRetrieveTarget();
        return target != null;
    }

    @Override
    public boolean shouldContinueExecuting() {
        return shouldExecute();
    }

    @Override
    public void startExecuting() {
        repathCooldown = 0;
        WolfPackHandler.setStalking(wolf, true);
    }

    @Override
    public void resetTask() {
        target = null;
        wolf.setSitting(false);
        wolf.setBegging(false);
        WolfPackHandler.setStalking(wolf, false);
        wolf.getNavigator().clearPath();
    }

    @Override
    public void updateTask() {
        if (target == null) return;
        wolf.getLookHelper().setLookPositionWithEntity(target, 10.0F, (float) wolf.getVerticalFaceSpeed());

        double sit = ForgeConfigHandler.wolfpack.retrievalSitDistance;
        double distSq = wolf.getDistanceSq(target);
        if (distSq <= sit * sit || (wolf.isSitting() && distSq <= (sit + STAND_UP_MARGIN) * (sit + STAND_UP_MARGIN))) {
            wolf.getNavigator().clearPath();
            if (!wolf.isSitting()) wolf.setSitting(true);
            wolf.setBegging(true);
            return;
        }

        if (wolf.isSitting()) wolf.setSitting(false);
        wolf.setBegging(false);
        if (--repathCooldown > 0) return;
        repathCooldown = REPATH_INTERVAL;
        wolf.getNavigator().tryMoveToEntityLiving(target, ForgeConfigHandler.wolfpack.stalkSpeed);
    }
}
