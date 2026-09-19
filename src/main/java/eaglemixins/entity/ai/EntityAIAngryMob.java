package eaglemixins.entity.ai;

import eaglemixins.config.ForgeConfigHandler;
import eaglemixins.config.folders.AngryMobConfig;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Target AI added to every untamed eligible creature. While the "Angry Mobs" config gives {@code minecraft:wolf}
 * a chance &gt; 0, each time a player comes into the mobs's view it rolls that chance once; on a hit
 * the mob targets the nearest visible player (which makes {@link EntityCreature#setAttackTarget} flip
 * it to angry) and hunts until line of sight is lost for {@code loseSightTicks}, then calms down.
 */
public class EntityAIAngryMob extends EntityAINearestAttackableTarget<EntityPlayer> {
    private final EntityCreature creature;
    private final Set<EntityPlayer> rolledThisSighting = new HashSet<>();
    private final AngryMobConfig.Entry cfgEntry;

    public EntityAIAngryMob(EntityCreature creature, AngryMobConfig.Entry cfgEntry) {
        super(creature, EntityPlayer.class, 0, true, false, null);
        this.creature = creature;
        this.cfgEntry = cfgEntry;
    }

    @Override
    public boolean shouldExecute() {
        if (cfgEntry == null || cfgEntry.chance <= 0.0F) return false;
        if (creature instanceof EntityTameable && ((EntityTameable) creature).isTamed()) return false;
        if (creature.isChild() ? !cfgEntry.baby : !cfgEntry.adult) return false;

        List<EntityPlayerMP> visible = creature.world.getPlayers(EntityPlayerMP.class, this.targetEntitySelector);
        visible.sort(this.sorter);
        rolledThisSighting.retainAll(visible);

        for (EntityPlayer player : visible) {
            if (!rolledThisSighting.add(player)) continue; // already rolled this sighting
            if (creature.getRNG().nextFloat() < cfgEntry.chance) {
                this.targetEntity = player;
                return true;
            }
        }
        return false;
    }

    @Override // used by targetEntitySelector
    public boolean isSuitableTarget(@Nullable EntityLivingBase target, boolean includeInvincibles) {
        if (target == null) return false;
        double followRange = this.getTargetDistance();
        if (this.creature.getDistanceSq(target) >= followRange * followRange) return false;
        return super.isSuitableTarget(target, includeInvincibles);
    }

    @Override
    public void startExecuting() {
        setUnseenMemoryTicks(ForgeConfigHandler.angrymobs.loseSightTicks);
        super.startExecuting();
    }

    @Override
    public boolean shouldContinueExecuting() {
        if (creature instanceof EntityTameable && ((EntityTameable) creature).isTamed()) return false;
        return super.shouldContinueExecuting();
    }

    @Override
    public void resetTask() {
        super.resetTask();
        rolledThisSighting.clear();
    }
}
