package eaglemixins.entity.ai;

import eaglemixins.config.ForgeConfigHandler;
import eaglemixins.config.folders.WolfPackConfig;
import eaglemixins.handlers.WolfPackDebug;
import eaglemixins.handlers.WolfPackHandler;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Wolf pack hunting. The alpha rolls "Hunt Chance Per Sighting" once per player sighting, but only while the
 * "Hunt Condition" allows it (always / night / cold / night or cold). A hit first starts a stalking phase
 * ("Stalk Seconds", movement in {@link EntityAIWolfStalk}), after it the alpha attacks.
 * Pack members (see {@link WolfPackHandler#getPackAlphaId}) don't pick targets themselves: they stalk with their alpha and
 * copy its attack target (see WolfPackHandler).
 * A player holding a calming item (bone) when the hunt starts or while being stalked gets a tribute instead: the alpha
 * comes over for its bones ("Retrieval Seconds", movement in {@link EntityAIWolfRetrieve}), and attacks if it doesn't get
 * enough in time. A satisfied alpha walks off ({@link EntityAIWolfWalkAway}), and giving it every calming item tames it.
 * Also holds the alpha rivalry state (duel against another alpha, fleeing after a lost duel), run by {@link WolfPackHandler}.
 */
public class EntityAIWolfPack extends EntityAINearestAttackableTarget<EntityPlayer> {
    private static final int PACK_CHECK_INTERVAL = 40;
    /** Ticks a satisfied alpha walks on at least after waiting for taming items */
    private static final long MIN_WALK_AFTER_WAIT = 60L;

    private final EntityWolf wolf;
    private final Set<EntityPlayer> rolledThisSighting = new HashSet<>();
    @Nullable private EntityWolf cachedAlpha = null;
    private int packCheckCooldown = 0;

    /** Player this wolf started a hunt on and is stalking, attacked once the world time reaches stalkEnd */
    @Nullable private EntityPlayer stalkTarget = null;
    private long stalkEnd = 0L;
    /** World time since when the stalked player is out of sight after the stalking / tribute time is up, 0 = in sight */
    private long unseenSince = 0L;

    // alpha rivalry
    @Nullable private EntityWolf rival = null;
    private long duelEnd = 0L;
    private long rivalCooldownUntil = 0L;
    private long nextRivalCheck = 0L;
    @Nullable private EntityWolf fleeFrom = null;
    private long fleeUntil = 0L;

    // tribute (alpha only): waiting for calming items from the stalked player, then satisfied with that player for a while
    private boolean retrieving = false;
    /** A tribute was started during this stalking (its progress and time left are kept when the player puts the bone away) */
    private boolean tributeStarted = false;
    /** Time left (ticks) of the mode that's paused: stalking while retrieving, retrieving while back to stalking */
    private long stalkRemaining = 0L;
    private long retrieveRemaining = 0L;
    private long retrieveEnd = 0L;
    /** How many of each item this tribute wants (rolled when the item is first given), and how far it is satisfied (1 = done) */
    private final Map<String, Integer> itemsWanted = new HashMap<>();
    private double tributeProgress = 0.0D;
    @Nullable private UUID satisfiedBy = null;
    private long satisfiedUntil = 0L;
    private double walkAwayAngle = 0.0D;
    /** Satisfied and given a taming item: sits facing that player until waitUntil, then walks on for walkRemaining ticks */
    @Nullable private EntityPlayer waitFor = null;
    private long waitUntil = 0L;
    private long walkRemaining = 0L;
    /** Calming items given while satisfied, every one of them tames it */
    private final Set<String> tameItems = new HashSet<>();
    /** Player the tameItems came from, kept until tameUntil ("Remember Target Seconds" after the last item, at least while satisfied) */
    @Nullable private UUID tamePlayer = null;
    private long tameUntil = 0L;

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
            if (stalkTarget != null) WolfPackDebug.log(wolf, "stalk of {} called off (tamed/child/duel/fleeing)", WolfPackDebug.describe(stalkTarget));
            stopStalking();
            return false;
        }

        if (stalkTarget != null) {
            if (wolf.getAttackTarget() != null || !isValidStalkTarget(stalkTarget)) {
                WolfPackDebug.log(wolf, "stalk of {} called off: attack target {}, valid {}", WolfPackDebug.describe(stalkTarget),
                        WolfPackDebug.describe(wolf.getAttackTarget()), isValidStalkTarget(stalkTarget));
                stopStalking(); // fighting something else (e.g. got hit) or the player got away
                rolledThisSighting.clear(); // roll again once the player is (still or again) in sight
            } else {
                long now = wolf.world.getTotalWorldTime();
                if (!retrieving && canRetrieve(stalkTarget, cfg)) {
                    startRetrieval(cfg, now); // pulled out a bone while stalked
                    WolfPackDebug.log(wolf, "{} pulled out a calming item: tribute, {} ticks left", WolfPackDebug.describe(stalkTarget), retrieveEnd - now);
                } else if (retrieving && !WolfPackHandler.isHoldingCalmingItem(stalkTarget)) {
                    stopRetrieval(now); // put it away: back to stalking
                    WolfPackDebug.log(wolf, "{} put the calming item away: back to stalking, {} ticks left", WolfPackDebug.describe(stalkTarget), stalkEnd - now);
                }
                if (now < (retrieving ? retrieveEnd : stalkEnd)) return false;
                if (!isSuitableTarget(stalkTarget, false)) {
                    // time is up but it can't see the player (hiding): give up after "Lose Sight Grace Ticks" instead of circling forever
                    if (unseenSince == 0L) unseenSince = now;
                    else if (now - unseenSince > cfg.loseSightTicks) {
                        WolfPackDebug.log(wolf, "lost sight of {} after the stalking: gives up", WolfPackDebug.describe(stalkTarget));
                        stopStalking();
                    }
                    return false;
                }
                WolfPackDebug.log(wolf, "{} over: attacks {}", retrieving ? "tribute time" : "stalking", WolfPackDebug.describe(stalkTarget));
                this.targetEntity = stalkTarget; // stalked long enough, or didn't get its tribute in time
                stopStalking();
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
        visible.removeIf(this::isSatisfiedWith); // got its tribute from them, leaves them alone for now

        // a player it attacked not long ago: no stalking, no roll, straight back to attacking (or to a tribute, with a bone)
        for (EntityPlayer player : visible) {
            if (isRemembered(player)) {
                rolledThisSighting.add(player);
                if (canRetrieve(player, cfg)) {
                    startStalking(player, cfg);
                    stalkRemaining = 0L; // remembered: putting the bone away means attacking right away, no stalking
                    WolfPackDebug.log(wolf, "remembers {}, holds a calming item: straight to tribute", WolfPackDebug.describe(player));
                    return false;
                }
                WolfPackDebug.log(wolf, "remembers {}: attacks right away", WolfPackDebug.describe(player));
                this.targetEntity = player;
                return true;
            }
        }

        if (cfg.huntChance <= 0.0F || !WolfPackHandler.isHuntingWeather(wolf)) return false;

        for (EntityPlayer player : visible) {
            if (!rolledThisSighting.add(player)) continue; // already rolled this sighting
            if (wolf.getRNG().nextFloat() < cfg.huntChance) {
                if (cfg.stalkSeconds > 0 || canRetrieve(player, cfg)) {
                    startStalking(player, cfg);
                    WolfPackDebug.log(wolf, "hunt roll hit {}: {}", WolfPackDebug.describe(player), retrieving ? "tribute (holds a calming item)" : "stalking");
                    return false;
                }
                WolfPackDebug.log(wolf, "hunt roll hit {}: attacks (no stalking)", WolfPackDebug.describe(player));
                this.targetEntity = player;
                return true;
            }
            WolfPackDebug.log(wolf, "hunt roll missed {}", WolfPackDebug.describe(player));
        }
        return false;
    }

    private void startStalking(EntityPlayer player, WolfPackConfig cfg) {
        long now = wolf.world.getTotalWorldTime();
        stalkTarget = player;
        stalkEnd = now + cfg.stalkSeconds * 20L;
        unseenSince = 0L;
        retrieving = false;
        tributeStarted = false;
        if (canRetrieve(player, cfg)) startRetrieval(cfg, now);
    }

    private void stopStalking() {
        stalkTarget = null;
        unseenSince = 0L;
        retrieving = false;
        tributeStarted = false;
    }

    private boolean canRetrieve(EntityPlayer player, WolfPackConfig cfg) {
        return cfg.retrievalSeconds > 0 && WolfPackHandler.isAlpha(wolf) && WolfPackHandler.isHoldingCalmingItem(player);
    }

    /**
     * Stalking pauses while the alpha waits for its tribute. The first time it's a new tribute, after the player put the bone
     * away and pulled it out again it carries on where it was (time left and items given), so switching doesn't buy time
     */
    private void startRetrieval(WolfPackConfig cfg, long now) {
        stalkRemaining = Math.max(0L, stalkEnd - now);
        retrieving = true;
        if (tributeStarted) {
            retrieveEnd = now + retrieveRemaining;
            return;
        }
        tributeStarted = true;
        retrieveEnd = now + cfg.retrievalSeconds * 20L;
        itemsWanted.clear();
        tributeProgress = 0.0D;
    }

    /** The player put the bone away before the alpha was satisfied: back to stalking for the stalking time that was left */
    private void stopRetrieval(long now) {
        retrieveRemaining = Math.max(0L, retrieveEnd - now);
        retrieving = false;
        stalkEnd = now + stalkRemaining;
    }

    /** Player the alpha is waiting on for its tribute (members keep stalking that player meanwhile) */
    @Nullable
    public EntityPlayer getRetrieveTarget() {
        if (!retrieving || stalkTarget == null || wolf.getAttackTarget() != null || rival != null || isFleeing()) return null;
        return isValidStalkTarget(stalkTarget) ? stalkTarget : null;
    }

    /** Satisfied with some player's tribute: walks off for a while */
    public boolean isSatisfied() {
        if (satisfiedBy == null) return false;
        if (wolf.world.getTotalWorldTime() < satisfiedUntil && wolf.getAttackTarget() == null && rival == null && !isFleeing()) return true;
        // "for now" is over: that player gets a new hunt roll, even if they never left its sight
        UUID done = satisfiedBy;
        rolledThisSighting.removeIf(player -> player.getUniqueID().equals(done));
        WolfPackDebug.log(wolf, "no longer satisfied (time up, or fighting)");
        satisfiedBy = null;
        waitFor = null;
        return false;
    }

    /** Player a satisfied alpha sits and waits for (after getting a taming item from them), or null while it walks */
    @Nullable
    public EntityPlayer getWaitTarget() {
        if (waitFor == null) return null;
        if (isSatisfied() && wolf.world.getTotalWorldTime() < waitUntil && waitFor.isEntityAlive() && waitFor.world == wolf.world) return waitFor;
        waitFor = null;
        return null;
    }

    private boolean isSatisfiedWith(EntityPlayer player) {
        return isSatisfied() && satisfiedBy.equals(player.getUniqueID());
    }

    /** Direction (radians) a satisfied alpha walks off in */
    public double getWalkAwayAngle() {
        return walkAwayAngle;
    }

    /** UNINTERESTED: satisfied, and already got this item type (a hint that other calming items exist) */
    public enum Tribute { REFUSED, UNINTERESTED, WANTS_MORE, SATISFIED, BONDING, TAMED }

    /**
     * The player gives the alpha a calming item. While waiting for a tribute it fills that item's share: the alpha wants a
     * random amount between min and max of it. Once satisfied, more different calming items can be given. Every different
     * calming item (from the tribute too) counts towards taming while the alpha remembers them, all of them tame it
     */
    public Tribute giveTribute(EntityPlayer player, String item, int min, int max, Set<String> allItems) {
        WolfPackConfig cfg = ForgeConfigHandler.wolfpack;
        long now = wolf.world.getTotalWorldTime();
        if (getRetrieveTarget() == player) {
            int lo = Math.max(1, min);
            int wanted = itemsWanted.computeIfAbsent(item, k -> lo + wolf.getRNG().nextInt(Math.max(lo, max) - lo + 1));
            tributeProgress += 1.0D / wanted;
            boolean tamed = addTameItem(player, item, allItems, now);
            if (tamed || tributeProgress >= 1.0D - 1.0E-6D) {
                stopStalking();
                satisfiedBy = player.getUniqueID();
                satisfiedUntil = now + cfg.satisfiedSeconds * 20L;
                tameUntil = Math.max(tameUntil, satisfiedUntil);
                walkAwayAngle = wolf.getRNG().nextDouble() * Math.PI * 2.0D;
                if (rememberedPlayer != null && rememberedPlayer.equals(player.getUniqueID())) rememberedPlayer = null;
                return tamed ? Tribute.TAMED : Tribute.SATISFIED;
            }
            retrieveEnd = now + cfg.retrievalSeconds * 20L;
            return Tribute.WANTS_MORE;
        }
        if (isSatisfiedWith(player)) {
            if (isTameItemGiven(player, item, now)) return Tribute.UNINTERESTED; // already got that one
            boolean tamed = addTameItem(player, item, allItems, now);
            // sit and wait for the next one, then walk on for the time it had left
            if (getWaitTarget() == null) walkRemaining = Math.max(satisfiedUntil - now, MIN_WALK_AFTER_WAIT);
            waitFor = player;
            waitUntil = now + cfg.tributeWaitSeconds * 20L;
            satisfiedUntil = waitUntil + walkRemaining;
            tameUntil = Math.max(tameUntil, satisfiedUntil);
            return tamed ? Tribute.TAMED : Tribute.BONDING;
        }
        return Tribute.REFUSED;
    }

    /** Clears the taming progress when it's from another player or the alpha forgot it */
    private void checkTameItems(EntityPlayer player, long now) {
        if (tamePlayer != null && tamePlayer.equals(player.getUniqueID()) && now <= tameUntil) return;
        tamePlayer = player.getUniqueID();
        tameItems.clear();
    }

    private boolean isTameItemGiven(EntityPlayer player, String item, long now) {
        checkTameItems(player, now);
        return tameItems.contains(item);
    }

    /** Counts the item towards taming, true once every calming item was given */
    private boolean addTameItem(EntityPlayer player, String item, Set<String> allItems, long now) {
        checkTameItems(player, now);
        tameItems.add(item);
        tameUntil = now + Math.max(0, ForgeConfigHandler.wolfpack.rememberTargetSeconds) * 20L;
        return tameItems.containsAll(allItems);
    }

    /** It attacks this player: the taming progress is gone */
    private void forgetTameItems(EntityPlayer player) {
        if (tamePlayer != null && tamePlayer.equals(player.getUniqueID())) {
            tamePlayer = null;
            tameItems.clear();
        }
    }

    /** Number of different calming items given since it was satisfied */
    public int getTameProgress() {
        return tameItems.size();
    }

    /** Player this wolf should be stalking right now: its own hunt, or the one its alpha is stalking (or waiting a tribute from) */
    @Nullable
    public EntityPlayer getStalkTarget() {
        WolfPackConfig cfg = ForgeConfigHandler.wolfpack;
        if (!cfg.enabled || wolf.isTamed() || wolf.isChild() || wolf.getAttackTarget() != null) return null;
        if (rival != null || isFleeing()) return null;
        if (stalkTarget != null) return !retrieving && isValidStalkTarget(stalkTarget) ? stalkTarget : null;
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
        stopStalking();
    }

    public void endDuel(long cooldownUntil) {
        this.rival = null;
        rolledThisSighting.clear();
        this.rivalCooldownUntil = cooldownUntil;
    }

    public void startFleeing(EntityWolf from, long until) {
        this.fleeFrom = from;
        this.fleeUntil = until;
        stopStalking();
    }

    // ---------- target task ----------

    @Override // used by targetEntitySelector
    public boolean isSuitableTarget(@Nullable EntityLivingBase target, boolean includeInvincibles) {
        if (!(target instanceof EntityPlayer)) return false;
        double followRange = this.getTargetDistance();
        if (wolf.getDistanceSq(target) >= followRange * followRange) return false;
        return super.isSuitableTarget(target, includeInvincibles);
    }

    /** One line of this AI's state, for the debug log */
    public String debugState() {
        long now = wolf.world.getTotalWorldTime();
        return String.format("stalk=%s%s (%d left) satisfied=%s wait=%s remembered=%s (%d left) rival=%s fleeing=%s rolled=%d tameItems=%s",
                WolfPackDebug.describe(stalkTarget), retrieving ? " RETRIEVING" : "", (retrieving ? retrieveEnd : stalkEnd) - now,
                satisfiedBy != null ? satisfiedBy.toString().substring(0, 8) : "-", WolfPackDebug.describe(waitFor),
                rememberedPlayer != null ? rememberedPlayer.toString().substring(0, 8) : "-", rememberUntil - now,
                WolfPackDebug.describe(rival), isFleeing(), rolledThisSighting.size(), tameItems);
    }

    @Override
    public void startExecuting() {
        WolfPackDebug.log(wolf, "pack target task starts attacking {}", WolfPackDebug.describe(this.targetEntity));
        setUnseenMemoryTicks(ForgeConfigHandler.wolfpack.loseSightTicks);
        if (this.targetEntity instanceof EntityPlayer) forgetTameItems((EntityPlayer) this.targetEntity); // attacks them: taming progress gone
        super.startExecuting();
    }

    @Override
    public boolean shouldContinueExecuting() {
        // calming items don't stop an attack, they only work before it (see the tribute)
        if (!ForgeConfigHandler.wolfpack.enabled || wolf.isTamed() || rival != null || isFleeing()) return false;
        return super.shouldContinueExecuting();
    }

    @Override
    public void resetTask() {
        // Vanilla clears the attack target unconditionally. When another target task (fight back when hurt) or a duel took
        // over, that would wipe its fresh target and the wolf's angry state, so only clear it if it's still our target
        EntityLivingBase current = wolf.getAttackTarget();
        WolfPackDebug.log(wolf, "pack target task stops (own target {}, current {})", WolfPackDebug.describe(this.target), WolfPackDebug.describe(current));
        // taken over by another target task (fight back when hit) with the same target: leave it, or the wolf calms down for a tick
        boolean takenOver = false;
        for (EntityAITasks.EntityAITaskEntry entry : wolf.targetTasks.taskEntries) {
            if (entry.using && entry.action != this) takenOver = true;
        }
        if (current == null || (current == this.target && !takenOver)) {
            super.resetTask(); // clears the attack target, which also makes the wolf stop being angry
        } else {
            this.target = null;
        }
        rolledThisSighting.clear();
    }
}
