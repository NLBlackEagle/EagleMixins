package eaglemixins.entity.ai;

import eaglemixins.config.ForgeConfigHandler;
import eaglemixins.config.folders.AngryMobConfig;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Target AI added to every untamed wolf. While the "Angry Mobs" config gives {@code minecraft:wolf}
 * a chance &gt; 0, each time a player comes into the wolf's view it rolls that chance once; on a hit
 * the wolf targets the nearest visible player (which makes {@link EntityWolf#setAttackTarget} flip
 * it to angry) and hunts until line of sight is lost for {@code loseSightTicks}, then calms down.
 */
public class EntityAIAngryWolf extends EntityAINearestAttackableTarget<EntityPlayer> {

    private final EntityWolf wolf;
    private final Set<UUID> rolledThisSighting = new HashSet<>();

    public EntityAIAngryWolf(EntityWolf wolf) {
        super(wolf, EntityPlayer.class, 0, true, false, null);
        this.wolf = wolf;
    }

    @Override
    public boolean shouldExecute() {
        AngryMobConfig.Entry entry = ForgeConfigHandler.angrymobs.getEntry(AngryMobConfig.WOLF);
        if (entry == null || entry.chance <= 0.0F) return false;
        if (wolf.isTamed()) return false;
        if (wolf.isChild() ? !entry.baby : !entry.adult) return false;

        List<EntityPlayer> visible = wolf.world.getEntitiesWithinAABB(
                EntityPlayer.class, getTargetableArea(getTargetDistance()), this.targetEntitySelector);
        if (visible.isEmpty()) {
            rolledThisSighting.clear();
            return false;
        }

        Set<UUID> visibleIds = new HashSet<>();
        for (EntityPlayer player : visible) visibleIds.add(player.getUniqueID());
        rolledThisSighting.retainAll(visibleIds);

        visible.sort(Comparator.comparingDouble(wolf::getDistanceSq));
        for (EntityPlayer player : visible) {
            if (!rolledThisSighting.add(player.getUniqueID())) continue; // already rolled this sighting
            if (wolf.getRNG().nextFloat() < entry.chance) {
                this.targetEntity = player;
                return true;
            }
        }
        return false;
    }

    @Override
    public void startExecuting() {
        setUnseenMemoryTicks(ForgeConfigHandler.angrymobs.loseSightTicks);
        super.startExecuting();
    }

    @Override
    public boolean shouldContinueExecuting() {
        if (wolf.isTamed()) return false;
        return super.shouldContinueExecuting();
    }

    @Override
    public void resetTask() {
        super.resetTask();
        rolledThisSighting.clear();
    }
}
