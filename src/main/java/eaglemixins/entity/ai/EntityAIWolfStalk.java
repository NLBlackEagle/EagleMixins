package eaglemixins.entity.ai;

import eaglemixins.config.ForgeConfigHandler;
import eaglemixins.config.folders.WolfPackConfig;
import eaglemixins.handlers.WolfPackHandler;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Movement while a wolf pack stalks a player (see {@link EntityAIWolfPack}): keep "Stalk Distance" blocks away from the
 * player and watch them. Wolves approach from where they are, so a pack ends up spread around the player.
 */
public class EntityAIWolfStalk extends EntityAIBase {
    private static final int REPATH_INTERVAL = 10;
    private static final double DISTANCE_TOLERANCE = 1.5D;
    /** Min blocks between two stalking wolves */
    private static final double SPACING = 2.5D;
    /** Angles (radians) tried around the player, from the wolf's own side outwards */
    private static final double[] ANGLE_OFFSETS = {0.0D, 0.6D, -0.6D, 1.2D, -1.2D, 1.8D, -1.8D, 2.4D, -2.4D, Math.PI};

    private final EntityWolf wolf;
    private final EntityAIWolfPack pack;
    @Nullable private EntityPlayer target = null;
    private int repathCooldown = 0;

    public EntityAIWolfStalk(EntityWolf wolf, EntityAIWolfPack pack) {
        this.wolf = wolf;
        this.pack = pack;
        this.setMutexBits(3); // move + look
    }

    @Override
    public boolean shouldExecute() {
        if (wolf.isSitting()) return false;
        target = pack.getStalkTarget();
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
        WolfPackHandler.setStalking(wolf, false);
        wolf.getNavigator().clearPath();
    }

    @Override
    public void updateTask() {
        if (target == null) return;
        wolf.getLookHelper().setLookPositionWithEntity(target, 30.0F, 30.0F);
        if (--repathCooldown > 0) return;
        repathCooldown = REPATH_INTERVAL;

        WolfPackConfig cfg = ForgeConfigHandler.wolfpack;
        double dx = wolf.posX - target.posX;
        double dz = wolf.posZ - target.posZ;
        double dist = Math.sqrt(dx * dx + dz * dz);
        double baseAngle = dist < 0.01D ? wolf.getRNG().nextDouble() * Math.PI * 2.0D : Math.atan2(dz, dx);

        // the point "Stalk Distance" blocks from the player on the wolf's side, so it moves closer or backs off.
        // If another wolf already stands there, swing around the player to a free spot, so the pack doesn't pile up
        List<EntityWolf> others = wolf.world.getEntitiesWithinAABB(EntityWolf.class,
                target.getEntityBoundingBox().grow(cfg.stalkDistance + SPACING + 1.0D), other -> other != null && other != wolf);
        double x = 0.0D, z = 0.0D;
        for (double offset : ANGLE_OFFSETS) {
            double angle = baseAngle + offset;
            x = target.posX + Math.cos(angle) * cfg.stalkDistance;
            z = target.posZ + Math.sin(angle) * cfg.stalkDistance;
            if (isFree(others, x, z)) break;
        }
        if (Math.abs(dist - cfg.stalkDistance) < DISTANCE_TOLERANCE && wolf.getDistanceSq(x, wolf.posY, z) < SPACING * SPACING) {
            wolf.getNavigator().clearPath(); // already on its spot
            return;
        }
        wolf.getNavigator().tryMoveToXYZ(x, target.posY, z, cfg.stalkSpeed);
    }

    private static boolean isFree(List<EntityWolf> others, double x, double z) {
        for (EntityWolf other : others) {
            double ox = other.posX - x;
            double oz = other.posZ - z;
            if (ox * ox + oz * oz < SPACING * SPACING) return false;
        }
        return true;
    }
}
