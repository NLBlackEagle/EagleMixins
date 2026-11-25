package eaglemixins.entity.ai;

import eaglemixins.config.ForgeConfigHandler;
import eaglemixins.config.folders.WolfPackConfig;
import eaglemixins.handlers.WolfPackHandler;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Wolf pack hunting. The alpha rolls "Hunt Chance Per Sighting" once per player sighting, but only while the
 * "Hunt Condition" allows it (always / night / cold / night or cold). A hit first starts a stalking phase
 * ("Stalk Seconds", movement in {@link EntityAIWolfStalk}), after it the alpha attacks.
 * Pack members (see {@link WolfPackHandler#getPackAlphaId}) don't pick targets themselves: they stalk with their alpha and
 * copy its attack target (see WolfPackHandler).
 * Players holding a calming item (bone) are never hunted, pulling one out makes the wolf calm down.
 * Also holds the alpha rivalry state (duel against another alpha, fleeing after a lost duel), run by {@link WolfPackHandler}.
 */
public class EntityAIWolfPack extends EntityAINearestAttackableTarget<EntityPlayer> {
    private static final int PACK_CHECK_INTERVAL = 40;

    private final EntityWolf wolf;
    private final Set<EntityPlayer> rolledThisSighting = new HashSet<>();
    @Nullable private EntityWolf cachedAlpha = null;
    private int packCheckCooldown = 0;

    /** Player this wolf started a hunt on and is stalking, attacked once the world time reaches stalkEnd */
    @Nullable private EntityPlayer stalkTarget = null;
    private long stalkEnd = 0L;

    // alpha rivalry
    @Nullable private EntityWolf rival = null;
    private long duelEnd = 0L;
    private long rivalCooldownUntil = 0L;
    private long nextRivalCheck = 0L;
    @Nullable private EntityWolf fleeFrom = null;
    private long fleeUntil = 0L;

    /** Alpha only: the player it attacked last (by UUID, so it survives respawning), attacked on sight until rememberUntil */
    @Nullable private UUID rememberedPlayer = null;
    private long rememberUntil = 0L;
    /** Alpha only: whether it attacked a player at the last check, to notice when it stops */
    private boolean hadPlayerAggro = false;

    public EntityAIWolfPack(EntityWolf wolf) {
        super(wolf, EntityPlayer.class, 0, true, false, null);
        this.wolf = wolf;
    }

    @Override
    public boolean shouldExecute() {
        WolfPackConfig cfg = ForgeConfigHandler.wolfpack;
        if (!cfg.enabled || wolf.isTamed() || wolf.isChild() || rival != null || isFleeing()) {
            stalkTarget = null;
            return false;
        }

        if (stalkTarget != null) {
            if (wolf.getAttackTarget() != null || !isValidStalkTarget(stalkTarget)) {
                stalkTarget = null; // fighting something else (e.g. got hit) or the player got away / pulled out a bone
                rolledThisSighting.clear(); // roll again once the player is (still or again) in sight
            } else {
                if (wolf.world.getTotalWorldTime() < stalkEnd || !isSuitableTarget(stalkTarget, false)) return false;
                this.targetEntity = stalkTarget;
                stalkTarget = null;
                return true;
            }
        }

        if (WolfPackHandler.isAlpha(wolf)) return rollForPlayer(cfg);

        // pack members don't pick targets themselves, they copy their alpha (see WolfPackHandler). Wolves without an alpha stay vanilla
        return false;
    }

    private boolean rollForPlayer(WolfPackConfig cfg) {
        List<EntityPlayerMP> visible = wolf.world.getPlayers(EntityPlayerMP.class, this.targetEntitySelector);
        visible.sort(this.sorter);
        rolledThisSighting.retainAll(visible);

        // a player it attacked not long ago: no stalking, no roll, straight back to attacking
        for (EntityPlayer player : visible) {
            if (isRemembered(player)) {
                this.targetEntity = player;
                return true;
            }
        }

        if (cfg.huntChance <= 0.0F || !WolfPackHandler.isHuntingWeather(wolf)) return false;

        for (EntityPlayer player : visible) {
            if (!rolledThisSighting.add(player)) continue; // already rolled this sighting
            if (wolf.getRNG().nextFloat() < cfg.huntChance) {
                if (cfg.stalkSeconds > 0) {
                    stalkTarget = player;
                    stalkEnd = wolf.world.getTotalWorldTime() + cfg.stalkSeconds * 20L;
                    return false;
                }
                this.targetEntity = player;
                return true;
            }
        }
        return false;
    }

    /** Player this wolf should be stalking right now: its own hunt, or the one its alpha is stalking */
    @Nullable
    public EntityPlayer getStalkTarget() {
        WolfPackConfig cfg = ForgeConfigHandler.wolfpack;
        if (!cfg.enabled || wolf.isTamed() || wolf.isChild() || wolf.getAttackTarget() != null) return null;
        if (rival != null || isFleeing()) return null;
        if (stalkTarget != null) return isValidStalkTarget(stalkTarget) ? stalkTarget : null;
        if (WolfPackHandler.isAlpha(wolf)) return null;

        EntityWolf alpha = getPackAlpha();
        if (alpha == null || wolf.getDistanceSq(alpha) > cfg.packRadius * cfg.packRadius) return null;
        EntityAIWolfPack alphaAI = of(alpha);
        if (alphaAI == null || alphaAI.stalkTarget == null) return null;
        return isValidStalkTarget(alphaAI.stalkTarget) ? alphaAI.stalkTarget : null;
    }

    private boolean isValidStalkTarget(EntityPlayer player) {
        if (!player.isEntityAlive() || player.world != wolf.world) return false;
        if (player.isSpectator() || player.capabilities.disableDamage) return false;
        if (WolfPackHandler.isHoldingCalmingItem(player)) return false;
        double giveUp = ForgeConfigHandler.wolfpack.stalkGiveUpDistance;
        return wolf.getDistanceSq(player) <= giveUp * giveUp;
    }

    @Nullable
    public static EntityAIWolfPack of(EntityWolf wolf) {
        for (EntityAITasks.EntityAITaskEntry entry : wolf.targetTasks.taskEntries) {
            if (entry.action instanceof EntityAIWolfPack) return (EntityAIWolfPack) entry.action;
        }
        return null;
    }

    /** Alpha of this wolf's pack if it's loaded (null for alphas and wolves without a pack). Loners look for a pack with room */
    @Nullable
    public EntityWolf getPackAlpha() {
        if (wolf.world.isRemote) return null;
        UUID id = WolfPackHandler.getPackAlphaId(wolf);
        if (cachedAlpha != null && (id == null || !id.equals(cachedAlpha.getUniqueID()) || !WolfPackHandler.isValidAlpha(cachedAlpha, wolf))) {
            cachedAlpha = null;
        }
        if (cachedAlpha == null && --packCheckCooldown <= 0) {
            packCheckCooldown = PACK_CHECK_INTERVAL;
            cachedAlpha = WolfPackHandler.updatePackMembership(wolf);
        }
        return cachedAlpha;
    }

    /** Alpha only: remember this player until the given time (refreshed while attacking it) */
    public void rememberTarget(EntityPlayer player, long until) {
        rememberedPlayer = player.getUniqueID();
        rememberUntil = until;
    }

    private boolean isRemembered(EntityPlayer player) {
        if (rememberedPlayer == null) return false;
        if (wolf.world.getTotalWorldTime() > rememberUntil) {
            rememberedPlayer = null;
            return false;
        }
        return rememberedPlayer.equals(player.getUniqueID());
    }

    /** Forget which players were already rolled for, so a player in sight gets a new roll (hunt over, fight over, ...) */
    public void resetSightings() {
        rolledThisSighting.clear();
    }

    /** Alpha only: true once, when it went from attacking a player to not attacking one */
    public boolean lostPlayerAggro(boolean hasPlayerAggro) {
        boolean lost = hadPlayerAggro && !hasPlayerAggro;
        hadPlayerAggro = hasPlayerAggro;
        return lost;
    }

    // ---------- alpha rivalry ----------

    @Nullable
    public EntityWolf getRival() {
        return rival;
    }

    public boolean isFleeing() {
        if (fleeFrom == null) return false;
        if (wolf.world.getTotalWorldTime() < fleeUntil && fleeFrom.isEntityAlive()) return true;
        fleeFrom = null;
        return false;
    }

    @Nullable
    public EntityWolf getFleeFrom() {
        return isFleeing() ? fleeFrom : null;
    }

    /** Free for a duel: not dueling, not fleeing and not on cooldown */
    public boolean canDuel(long now) {
        return rival == null && !isFleeing() && now >= rivalCooldownUntil;
    }

    /** True once every interval, so alphas only look for rivals now and then */
    public boolean isRivalCheckDue(long now, int interval) {
        if (now < nextRivalCheck) return false;
        nextRivalCheck = now + interval;
        return true;
    }

    public boolean isDuelOverdue(long now) {
        return now > duelEnd;
    }

    public void startDuel(EntityWolf rival, long duelEnd) {
        this.rival = rival;
        this.duelEnd = duelEnd;
        this.stalkTarget = null;
    }

    public void endDuel(long cooldownUntil) {
        this.rival = null;
        rolledThisSighting.clear();
        this.rivalCooldownUntil = cooldownUntil;
    }

    public void startFleeing(EntityWolf from, long until) {
        this.fleeFrom = from;
        this.fleeUntil = until;
        this.stalkTarget = null;
    }

    // ---------- target task ----------

    @Override // used by targetEntitySelector
    public boolean isSuitableTarget(@Nullable EntityLivingBase target, boolean includeInvincibles) {
        if (!(target instanceof EntityPlayer)) return false;
        if (WolfPackHandler.isHoldingCalmingItem((EntityPlayer) target)) return false;
        double followRange = this.getTargetDistance();
        if (wolf.getDistanceSq(target) >= followRange * followRange) return false;
        return super.isSuitableTarget(target, includeInvincibles);
    }

    @Override
    public void startExecuting() {
        setUnseenMemoryTicks(ForgeConfigHandler.wolfpack.loseSightTicks);
        super.startExecuting();
    }

    @Override
    public boolean shouldContinueExecuting() {
        if (!ForgeConfigHandler.wolfpack.enabled || wolf.isTamed() || rival != null || isFleeing()) return false;
        EntityLivingBase target = wolf.getAttackTarget();
        if (target instanceof EntityPlayer && WolfPackHandler.isHoldingCalmingItem((EntityPlayer) target)) return false;
        return super.shouldContinueExecuting();
    }

    @Override
    public void resetTask() {
        // Vanilla clears the attack target unconditionally. When another target task (fight back when hurt) or a duel took
        // over, that would wipe its fresh target and the wolf's angry state, so only clear it if it's still our target
        EntityLivingBase current = wolf.getAttackTarget();
        if (current == null || current == this.target) {
            super.resetTask(); // clears the attack target, which also makes the wolf stop being angry
        } else {
            this.target = null;
        }
        rolledThisSighting.clear();
    }
}
