package eaglemixins.handlers;

import eaglemixins.compat.ModLoadedUtil;
import eaglemixins.compat.SimpleDifficultyTemperature;
import eaglemixins.config.ForgeConfigHandler;
import eaglemixins.config.folders.WolfPackConfig;
import eaglemixins.entity.ai.EntityAIPackGate;
import eaglemixins.entity.ai.EntityAIWolfFlee;
import eaglemixins.entity.ai.EntityAIWolfFollowAlpha;
import eaglemixins.entity.ai.EntityAIWolfPack;
import eaglemixins.entity.ai.EntityAIWolfRetrieve;
import eaglemixins.entity.ai.EntityAIWolfStalk;
import eaglemixins.entity.ai.EntityAIWolfWalkAway;
import eaglemixins.network.PacketHandler;
import eaglemixins.network.PacketSyncAlphaWolf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAITargetNonTamed;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.UUID;
import java.util.WeakHashMap;

/**
 * Wolf packs: every wild pack gets a fixed alpha (entity tag, saved with the wolf). The alpha is stronger, has its own skin,
 * and decides when the pack hunts players (see {@link EntityAIWolfPack}). It can't be tamed the vanilla way, only through
 * its tribute; a tamed alpha keeps its skin and health.
 * Wild wolves deal less damage to players.
 */
public class WolfPackHandler {
    public static final String ALPHA_TAG = "eaglemixins:alpha_wolf";
    /** Alpha is stalking a player, synced to clients for the stalking skin. Cleared on load in case it was saved mid-stalk */
    public static final String STALKING_TAG = "eaglemixins:alpha_stalking";
    private static final String CHECKED_TAG = "eaglemixins:wolf_pack_checked";
    private static final ResourceLocation WOLF_ID = new ResourceLocation("minecraft:wolf");
    /** Vanilla wolf: 3 avoid llamas, 4 melee attack, 7 wander. Stalking only runs without an attack target, so it never blocks the melee */
    private static final int STALK_PRIORITY = 4;
    /** Above everything but swimming, so a beaten alpha really runs */
    private static final int FLEE_PRIORITY = 1;
    /** Vanilla wolf: 6 mate, 7 wander */
    private static final int FOLLOW_ALPHA_PRIORITY = 6;

    /** ForgeData key holding the UUID of the wolf's pack alpha */
    private static final String PACK_ALPHA_KEY = "EagleMixinsPackAlpha";
    /** How far around an alpha its pack members are counted / looked for */
    private static final double PACK_SCAN_RADIUS = 48.0D;
    private static final int RIVAL_CHECK_INTERVAL = 40;
    private static final long MAX_DUEL_TICKS = 20L * 30L;
    /** Wolves that targeted an alpha in a duel (or a fleeing one) they may not attack. Cleared on their next update */
    private static final Set<EntityWolf> PENDING_TARGET_CLEAR = Collections.newSetFromMap(new WeakHashMap<>());

    private static final UUID HEALTH_UUID = UUID.fromString("6f0c2b4e-3d55-4a52-9a43-0a1f5e7d6b11");
    private static final UUID FOLLOW_RANGE_UUID = UUID.fromString("6f0c2b4e-3d55-4a52-9a43-0a1f5e7d6b14");
    /** Old alpha speed modifier, saved on alphas from earlier versions. Only removed */
    private static final UUID OLD_ALPHA_SPEED_UUID = UUID.fromString("6f0c2b4e-3d55-4a52-9a43-0a1f5e7d6b12");
    private static final UUID ANGRY_SPEED_UUID = UUID.fromString("6f0c2b4e-3d55-4a52-9a43-0a1f5e7d6b13");
    private static final int ANGRY_SPEED_CHECK_INTERVAL = 5;
    /** Lang keys eaglemixins.wolfpack.bonding.1 to .N, one picked at random when an item counts towards taming */
    private static final int BONDING_MESSAGES = 3;

    public static boolean isWolf(Entity entity) {
        return entity instanceof EntityWolf && WOLF_ID.equals(EntityList.getKey(entity));
    }

    public static boolean isAlpha(Entity entity) {
        return entity.getTags().contains(ALPHA_TAG);
    }

    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (event.getWorld().isRemote) return;
        WolfPackConfig cfg = ForgeConfigHandler.wolfpack;
        if (!cfg.enabled) return;
        if (!isWolf(event.getEntity())) return;
        EntityWolf wolf = (EntityWolf) event.getEntity();

        boolean newAlpha = false;
        if (wolf.addTag(CHECKED_TAG) && !wolf.isTamed() && !wolf.isChild()) { // first time this wolf is seen
            if (findNearestAlpha(wolf, cfg.alphaSearchRadius) == null && wolf.getRNG().nextDouble() < cfg.alphaChance) {
                wolf.addTag(ALPHA_TAG);
                newAlpha = true;
                WolfPackDebug.log(wolf, "new wolf became an alpha");
            } else {
                EntityWolf joined = findJoinableAlpha(wolf, cfg.alphaSearchRadius);
                setPackAlpha(wolf, joined);
                WolfPackDebug.log(wolf, "new wolf joined {}", WolfPackDebug.describe(joined));
            }
        }
        if (isAlpha(wolf) || wolf.isTamed()) setPackAlpha(wolf, null);
        wolf.removeTag(STALKING_TAG); // saved mid-stalk

        applyAlphaStats(wolf, newAlpha);

        if (wolf.targetTasks.taskEntries.stream().noneMatch(t -> t.action instanceof EntityAIWolfPack)) {
            EntityAIWolfPack pack = new EntityAIWolfPack(wolf);
            wolf.targetTasks.addTask(cfg.aiPriority, pack);
            wolf.tasks.addTask(STALK_PRIORITY, new EntityAIWolfStalk(wolf, pack));
            wolf.tasks.addTask(STALK_PRIORITY, new EntityAIWolfRetrieve(wolf, pack));
            wolf.tasks.addTask(STALK_PRIORITY, new EntityAIWolfWalkAway(wolf, pack));
            wolf.tasks.addTask(FLEE_PRIORITY, new EntityAIWolfFlee(wolf, pack));
            wolf.tasks.addTask(FOLLOW_ALPHA_PRIORITY, new EntityAIWolfFollowAlpha(wolf, pack));
            gateOwnTargeting(wolf);
        }
    }

    /** Angry wild wolves are faster while their player target is further away than "Angry Speed Min Distance" */
    @SubscribeEvent
    public static void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        if (entity.getClass() != EntityWolf.class || entity.world.isRemote) return;
        EntityWolf wolf = (EntityWolf) entity;
        if (!PENDING_TARGET_CLEAR.isEmpty() && PENDING_TARGET_CLEAR.remove(wolf)) {
            EntityLivingBase target = wolf.getAttackTarget();
            if (target instanceof EntityWolf && isProtectedFrom((EntityWolf) target, wolf)) wolf.setAttackTarget(null);
        }
        if (entity.ticksExisted % ANGRY_SPEED_CHECK_INTERVAL != 0) return;
        WolfPackConfig cfg = ForgeConfigHandler.wolfpack;
        if (cfg.enabled && wolf.isInLove() && isHunting(wolf)) wolf.resetInLove(); // no breeding mid-hunt

        // alphas and pack members get the pack behaviour, wolves without an alpha stay vanilla
        boolean alpha = cfg.enabled && !wolf.isTamed() && isAlpha(wolf);
        EntityWolf packAlpha = null;
        if (cfg.enabled && !wolf.isTamed() && !alpha) {
            EntityAIWolfPack ownPack = EntityAIWolfPack.of(wolf);
            if (ownPack != null) packAlpha = ownPack.getPackAlpha();
        }
        boolean inPack = alpha || packAlpha != null;
        if (!isAlpha(wolf)) setFollowRange(wolf, packAlpha != null ? cfg.memberFollowRange : -1.0D);
        if (inPack && wolf.isAngry() && wolf.getAttackTarget() == null) {
            WolfPackDebug.log(wolf, "angry without a target: calmed down");
            wolf.setAngry(false);
        }
        if (packAlpha != null) copyAlpha(wolf, packAlpha);
        else if (alpha) dropOutOfRangeTarget(wolf);
        else if (cfg.enabled && !wolf.isTamed()) dropDeadTarget(wolf); // loners stay vanilla, but don't stay angry at a dead player
        if (WolfPackDebug.on() && wolf.ticksExisted % WolfPackDebug.DUMP_INTERVAL == 0
                && (wolf.isAngry() || wolf.getAttackTarget() != null || wolf.getTags().contains(STALKING_TAG))) {
            WolfPackDebug.dump(wolf);
        }
        if (cfg.enabled && isAlpha(wolf) && !wolf.isTamed()) {
            EntityAIWolfPack pack = EntityAIWolfPack.of(wolf);
            // saved while sitting for a tribute: a wild wolf has nothing else that makes it stand up
            if (wolf.isSitting() && (pack == null || (pack.getRetrieveTarget() == null && pack.getWaitTarget() == null))) wolf.setSitting(false);
            if (pack != null) {
                if (wolf.getAttackTarget() instanceof EntityPlayer && cfg.rememberTargetSeconds > 0) {
                    pack.rememberTarget((EntityPlayer) wolf.getAttackTarget(), wolf.world.getTotalWorldTime() + cfg.rememberTargetSeconds * 20L);
                }
                if (pack.lostPlayerAggro(wolf.getAttackTarget() instanceof EntityPlayer)) {
                    WolfPackDebug.log(wolf, "alpha stopped attacking players: calming the pack");
                    pack.resetSightings(); // back to stalking once it sees the player again
                    calmPack(wolf);
                }
                tickRivalry(wolf, pack, cfg);
            }
        }

        IAttributeInstance speed = wolf.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
        if (speed == null) return;

        boolean fast = false;
        if (inPack && cfg.angrySpeedBonus > 0.0D && wolf.isAngry()) {
            EntityLivingBase target = wolf.getAttackTarget();
            fast = target instanceof EntityPlayer
                    && wolf.getDistanceSq(target) > cfg.angrySpeedMinDistance * cfg.angrySpeedMinDistance;
        }

        AttributeModifier current = speed.getModifier(ANGRY_SPEED_UUID);
        if (fast) {
            if (current != null && current.getAmount() == cfg.angrySpeedBonus) return;
            if (current != null) speed.removeModifier(ANGRY_SPEED_UUID);
            speed.applyModifier(new AttributeModifier(ANGRY_SPEED_UUID, "EagleMixins angry wolf speed", cfg.angrySpeedBonus, 2).setSaved(false));
        } else if (current != null) {
            speed.removeModifier(ANGRY_SPEED_UUID);
        }
    }

    /** Other wild wolves may not join a duel between two alphas, nor go after a fleeing alpha (vanilla calls the whole pack for help) */
    @SubscribeEvent
    public static void onSetAttackTarget(LivingSetAttackTargetEvent event) {
        EntityLivingBase target = event.getTarget();
        EntityLivingBase entity = event.getEntityLiving();
        if (WolfPackDebug.on() && entity instanceof EntityWolf && !entity.world.isRemote && !((EntityWolf) entity).isTamed()) {
            WolfPackDebug.log((EntityWolf) entity, "attack target -> {} by {}", WolfPackDebug.describe(target), WolfPackDebug.caller());
        }
        if (!(target instanceof EntityWolf) || !(entity instanceof EntityWolf) || entity.world.isRemote) return;
        if (((EntityWolf) entity).isTamed()) return;
        // can't clear it from inside setAttackTarget (the wolf would stay angry), so on its next update
        if (isProtectedFrom((EntityWolf) target, (EntityWolf) entity)) PENDING_TARGET_CLEAR.add((EntityWolf) entity);
    }

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        EntityLivingBase victim = event.getEntityLiving();
        if (!(victim instanceof EntityWolf) || victim.world.isRemote) return;
        Entity attacker = event.getSource().getTrueSource();
        if (!(attacker instanceof EntityWolf) || ((EntityWolf) attacker).isTamed()) return;
        if (isProtectedFrom((EntityWolf) victim, (EntityWolf) attacker)) event.setCanceled(true);
    }

    /**
     * Hunting wild wolves can't be fed into love mode (Wolf Armor and Storage lets wild wolves breed). Calming items given to
     * an alpha are its tribute (see {@link EntityAIWolfPack#giveTribute}), never the vanilla bone taming
     */
    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        WolfPackConfig cfg = ForgeConfigHandler.wolfpack;
        if (!cfg.enabled) return;
        Entity target = event.getTarget();
        if (!isWolf(target)) return;
        EntityWolf wolf = (EntityWolf) target;
        if (wolf.isTamed()) return;
        ItemStack stack = event.getItemStack();

        if (wolf.isBreedingItem(stack) && isHunting(wolf)) {
            event.setCanceled(true);
            event.setCancellationResult(EnumActionResult.FAIL);
            return;
        }

        if (!isAlpha(wolf) || !(isCalmingItem(stack) || stack.getItem() == Items.BONE)) return;
        event.setCanceled(true);
        event.setCancellationResult(EnumActionResult.SUCCESS);
        if (wolf.world.isRemote) return;

        EntityPlayer player = event.getEntityPlayer();
        EntityAIWolfPack pack = EntityAIWolfPack.of(wolf);
        String item = getItemId(stack);
        EntityAIWolfPack.Tribute result = pack == null || item == null || !isCalmingItem(stack) ? EntityAIWolfPack.Tribute.REFUSED
                : pack.giveTribute(player, item, getAmount(item, 1), getAmount(item, 2), getTameItems());
        WolfPackDebug.log(wolf, "{} gave {}: {} (taming items {})", WolfPackDebug.describe(player), item, result, pack != null ? pack.getTameProgress() : 0);
        if (result == EntityAIWolfPack.Tribute.REFUSED || result == EntityAIWolfPack.Tribute.UNINTERESTED) {
            wolf.world.setEntityState(wolf, (byte) 6); // smoke: doesn't want it, the item isn't used up
            if (result == EntityAIWolfPack.Tribute.UNINTERESTED) sendMessage(player, "eaglemixins.wolfpack.uninterested");
            return;
        }

        if (!player.capabilities.isCreativeMode) stack.shrink(1);
        switch (result) {
            case WANTS_MORE:
                sendMessage(player, "eaglemixins.wolfpack.unsatisfied");
                break;
            case SATISFIED:
                sendMessage(player, "eaglemixins.wolfpack.satisfied");
                break;
            case BONDING:
                spawnHearts(wolf, pack.getTameProgress());
                sendMessage(player, "eaglemixins.wolfpack.bonding." + (1 + wolf.getRNG().nextInt(BONDING_MESSAGES)));
                break;
            case TAMED:
                spawnHearts(wolf, pack.getTameProgress());
                tameAlpha(wolf, player);
                break;
            default:
                break;
        }
    }

    /** Tames the alpha like a vanilla bone would (sits down), but it keeps its alpha health and skin */
    private static void tameAlpha(EntityWolf wolf, EntityPlayer player) {
        if (ForgeEventFactory.onAnimalTame(wolf, player)) {
            WolfPackDebug.log(wolf, "taming by {} cancelled by another mod", WolfPackDebug.describe(player));
            return;
        }
        sendMessage(player, "eaglemixins.wolfpack.tamed");
        WolfPackDebug.log(wolf, "tamed by {}", WolfPackDebug.describe(player));
        setStalking(wolf, false);
        wolf.setTamedBy(player);
        wolf.getNavigator().clearPath();
        wolf.setAttackTarget(null);
        wolf.getAISit().setSitting(true);
        wolf.setBegging(false);
        for (EntityWolf member : getPackMembers(wolf)) setPackAlpha(member, null); // its pack goes on without it
    }

    private static void spawnHearts(EntityWolf wolf, int count) {
        if (count <= 0 || !(wolf.world instanceof WorldServer)) return;
        ((WorldServer) wolf.world).spawnParticle(EnumParticleTypes.HEART, wolf.posX, wolf.posY + wolf.height + 0.3D, wolf.posZ,
                count, wolf.width * 0.6D, 0.3D, wolf.width * 0.6D, 0.0D);
    }

    /** Shows a lang message (key from assets/eaglemixins/lang) above the player's hotbar */
    private static void sendMessage(EntityPlayer player, String langKey) {
        player.sendStatusMessage(new TextComponentTranslation(langKey), true);
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        WolfPackConfig cfg = ForgeConfigHandler.wolfpack;
        if (!cfg.enabled) return;
        Entity source = event.getSource().getTrueSource();
        if (!isWolf(source)) return;
        EntityWolf wolf = (EntityWolf) source;
        if (wolf.isTamed()) return;

        float amount = event.getAmount();
        if (isAlpha(wolf)) amount *= (float) cfg.alphaDamageMultiplier;
        if (event.getEntityLiving() instanceof EntityPlayer) amount *= (float) cfg.playerDamageMultiplier;
        event.setAmount(amount);
    }

    /** Tells clients which wolves are alphas, for the dark skin */
    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (!ForgeConfigHandler.wolfpack.enabled) return;
        Entity target = event.getTarget();
        if (!isWolf(target) || !isAlpha(target)) return;
        if (event.getEntityPlayer() instanceof EntityPlayerMP) {
            PacketHandler.sendTo(new PacketSyncAlphaWolf(target, target.getTags().contains(STALKING_TAG)), (EntityPlayerMP) event.getEntityPlayer());
        }
    }

    /** Marks an alpha as stalking (or not) and tells the clients tracking it, for the stalking skin */
    public static void setStalking(EntityWolf wolf, boolean stalking) {
        if (wolf.world.isRemote || !isAlpha(wolf)) return;
        boolean changed = stalking ? wolf.addTag(STALKING_TAG) : wolf.removeTag(STALKING_TAG);
        if (changed) PacketHandler.sendToAllTracking(new PacketSyncAlphaWolf(wolf, stalking), wolf);
    }

    // ---------- losing the target ----------

    /** Sets the wolf's follow range to this many blocks, or back to its own value with a negative range */
    private static void setFollowRange(EntityWolf wolf, double range) {
        IAttributeInstance followRange = wolf.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE);
        if (followRange == null) return;
        AttributeModifier current = followRange.getModifier(FOLLOW_RANGE_UUID);
        double extra = range < 0.0D ? 0.0D : range - followRange.getBaseValue();
        if (current != null && current.getAmount() == extra) return;
        if (current != null) followRange.removeModifier(FOLLOW_RANGE_UUID);
        if (extra != 0.0D) followRange.applyModifier(new AttributeModifier(FOLLOW_RANGE_UUID, "EagleMixins wolf pack follow range", extra, 0));
    }

    /**
     * Target dead, in another dimension or further away than the wolf's follow range: drop it, whatever gave it the target.
     * That also calms the wolf down, and pack members then walk back to their alpha
     */
    private static void dropOutOfRangeTarget(EntityWolf wolf) {
        EntityLivingBase target = wolf.getAttackTarget();
        if (target == null) return;
        IAttributeInstance followRange = wolf.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE);
        double range = followRange != null ? followRange.getAttributeValue() : 16.0D;
        boolean lost = !target.isEntityAlive() || target.world != wolf.world || wolf.getDistanceSq(target) > range * range;
        if (!lost) return;
        WolfPackDebug.log(wolf, "drops target {}: alive {}, same world {}, dist {} > range {}", WolfPackDebug.describe(target),
                target.isEntityAlive(), target.world == wolf.world, (int) wolf.getDistance(target), (int) range);
        clearTarget(wolf);
    }

    /** Dead target or one in another dimension: drop it (vanilla keeps it when another wolf alerted it, and stays angry) */
    private static void dropDeadTarget(EntityWolf wolf) {
        EntityLivingBase target = wolf.getAttackTarget();
        if (target == null || (target.isEntityAlive() && target.world == wolf.world)) return;
        WolfPackDebug.log(wolf, "drops dead/gone target {}", WolfPackDebug.describe(target));
        clearTarget(wolf);
    }

    /** Drops the attack target for good (a running target AI would set it again) and calms the wolf down */
    private static void clearTarget(EntityWolf wolf) {
        EntityLivingBase target = wolf.getAttackTarget();
        restartTargetTasks(wolf, action -> true);
        if (target != null && wolf.getRevengeTarget() == target) wolf.setRevengeTarget(null);
        wolf.setAttackTarget(null); // also calms it down
    }

    /**
     * The alpha is the pack's one source: a member attacks what its alpha attacks (if in its follow range), and is calm when
     * the alpha is. Duels stay between the alphas. A member that just got hit hands the fight to its alpha, so the pack follows
     */
    private static void copyAlpha(EntityWolf wolf, EntityWolf alpha) {
        EntityLivingBase alphaTarget = alpha.getAttackTarget();
        if (alphaTarget instanceof EntityWolf) alphaTarget = null; // alpha duels: the pack stays out of it

        EntityLivingBase revenge = wolf.getRevengeTarget();
        if (alphaTarget == null && revenge != null && !(revenge instanceof EntityWolf) && revenge.isEntityAlive()
                && wolf.ticksExisted - wolf.getRevengeTimer() <= ANGRY_SPEED_CHECK_INTERVAL * 2) {
            EntityAIWolfPack alphaPack = EntityAIWolfPack.of(alpha);
            if (alpha.getAttackTarget() == null && (alphaPack == null || (alphaPack.getRival() == null && !alphaPack.isFleeing()))) {
                WolfPackDebug.log(wolf, "got hit by {}: hands the fight to its alpha {}", WolfPackDebug.describe(revenge), WolfPackDebug.describe(alpha));
                alpha.setAttackTarget(revenge);
                alphaTarget = revenge;
            }
        }

        EntityLivingBase current = wolf.getAttackTarget();
        IAttributeInstance followRange = wolf.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE);
        double range = followRange != null ? followRange.getAttributeValue() : 16.0D;
        if (alphaTarget != null && alphaTarget.isEntityAlive() && alphaTarget.world == wolf.world
                && wolf.getDistanceSq(alphaTarget) <= range * range) {
            if (current != alphaTarget) {
                WolfPackDebug.log(wolf, "copies alpha {} target {}", WolfPackDebug.describe(alpha), WolfPackDebug.describe(alphaTarget));
                wolf.setAttackTarget(alphaTarget);
            }
        } else if (current != null) {
            WolfPackDebug.log(wolf, "alpha {} target is {}, drops own target {}", WolfPackDebug.describe(alpha), WolfPackDebug.describe(alpha.getAttackTarget()), WolfPackDebug.describe(current));
            clearTarget(wolf);
        }
    }

    /** Puts the wolf's own vanilla targeting (sheep/rabbits, skeletons) behind {@link EntityAIPackGate}, so pack members leave it to the alpha */
    private static void gateOwnTargeting(EntityWolf wolf) {
        List<EntityAITasks.EntityAITaskEntry> gate = new ArrayList<>();
        for (EntityAITasks.EntityAITaskEntry entry : wolf.targetTasks.taskEntries) {
            Class<?> type = entry.action.getClass();
            if (type == EntityAITargetNonTamed.class || type == EntityAINearestAttackableTarget.class) gate.add(entry);
        }
        for (EntityAITasks.EntityAITaskEntry entry : gate) {
            wolf.targetTasks.removeTask(entry.action);
            wolf.targetTasks.addTask(entry.priority, new EntityAIPackGate(wolf, entry.action));
        }
    }

    /** The alpha stopped attacking players: its whole pack drops its player targets, calms down and goes back to the alpha */
    private static void calmPack(EntityWolf alpha) {
        for (EntityWolf member : getPackMembers(alpha)) {
            EntityLivingBase target = member.getAttackTarget();
            if (!(target instanceof EntityPlayer)) continue;
            WolfPackDebug.log(member, "calmed by its alpha, drops {}", WolfPackDebug.describe(target));
            restartTargetTasks(member, action -> true);
            if (member.getRevengeTarget() == target) member.setRevengeTarget(null);
            member.setAttackTarget(null); // also calms it down
            EntityAIWolfPack memberPack = EntityAIWolfPack.of(member);
            if (memberPack != null) memberPack.resetSightings();
        }
    }

    /** Stops the matching running target tasks without them setting their target again, and puts them back */
    private static void restartTargetTasks(EntityWolf wolf, Predicate<EntityAIBase> filter) {
        List<EntityAITasks.EntityAITaskEntry> restart = new ArrayList<>();
        for (EntityAITasks.EntityAITaskEntry entry : wolf.targetTasks.taskEntries) {
            if (entry.using && filter.test(entry.action)) restart.add(entry);
        }
        for (EntityAITasks.EntityAITaskEntry entry : restart) {
            wolf.targetTasks.removeTask(entry.action);
            wolf.targetTasks.addTask(entry.priority, entry.action);
        }
    }

    // ---------- alpha rivalry ----------

    private static void tickRivalry(EntityWolf wolf, EntityAIWolfPack pack, WolfPackConfig cfg) {
        long now = wolf.world.getTotalWorldTime();
        EntityWolf rival = pack.getRival();
        if (rival != null) {
            tickDuel(wolf, pack, rival, cfg, now);
            return;
        }
        if (!cfg.alphaRivalry || wolf.isChild() || !pack.canDuel(now) || !pack.isRivalCheckDue(now, RIVAL_CHECK_INTERVAL)) return;
        if (isBusyFighting(wolf)) return;

        List<EntityWolf> rivals = wolf.world.getEntitiesWithinAABB(EntityWolf.class, wolf.getEntityBoundingBox().grow(cfg.rivalryRadius), other -> {
            if (other == null || other == wolf || !isValidAlpha(other, wolf) || other.isChild()) return false;
            EntityAIWolfPack otherPack = EntityAIWolfPack.of(other);
            return otherPack != null && otherPack.canDuel(now) && !isBusyFighting(other);
        });
        double radiusSq = cfg.rivalryRadius * cfg.rivalryRadius;
        rivals.removeIf(other -> wolf.getDistanceSq(other) > radiusSq || !wolf.canEntityBeSeen(other));
        if (rivals.isEmpty()) return;
        rivals.sort(Comparator.comparingDouble(wolf::getDistanceSq));
        EntityWolf other = rivals.get(0);
        EntityAIWolfPack otherPack = EntityAIWolfPack.of(other);
        if (otherPack == null) return;

        WolfPackDebug.log(wolf, "duel starts against {}", WolfPackDebug.describe(other));
        pack.startDuel(other, now + MAX_DUEL_TICKS);
        otherPack.startDuel(wolf, now + MAX_DUEL_TICKS);
        wolf.setAttackTarget(other);
        other.setAttackTarget(wolf);
    }

    private static void tickDuel(EntityWolf wolf, EntityAIWolfPack pack, EntityWolf rival, WolfPackConfig cfg, long now) {
        EntityAIWolfPack rivalPack = EntityAIWolfPack.of(rival);
        double maxDist = cfg.rivalryRadius * 2.0D;
        boolean valid = cfg.alphaRivalry && isValidAlpha(rival, wolf) && rivalPack != null && rivalPack.getRival() == wolf
                && wolf.getDistanceSq(rival) <= maxDist * maxDist && !pack.isDuelOverdue(now);
        EntityLivingBase target = wolf.getAttackTarget();
        if (valid && target != rival) {
            if (target == null) wolf.setAttackTarget(rival); // something cleared it, keep fighting
            else valid = false; // something else (a player) took over, call the fight off
        }
        if (!valid) {
            WolfPackDebug.log(wolf, "duel against {} called off (target now {})", WolfPackDebug.describe(rival), WolfPackDebug.describe(wolf.getAttackTarget()));
            endDuel(wolf, pack, rival, cfg, now);
            if (rivalPack != null && rivalPack.getRival() == wolf) endDuel(rival, rivalPack, wolf, cfg, now);
            return;
        }

        if (wolf.getHealth() > wolf.getMaxHealth() * cfg.rivalryFleeHealth) return;
        // lost: flee from the winner, the pack goes over to the winner
        endDuel(rival, rivalPack, wolf, cfg, now);
        endDuel(wolf, pack, rival, cfg, now);
        WolfPackDebug.log(wolf, "lost the duel against {}: flees, pack goes over", WolfPackDebug.describe(rival));
        pack.startFleeing(rival, now + cfg.rivalryFleeSeconds * 20L);
        transferPack(wolf, rival, cfg);
    }

    private static void endDuel(EntityWolf wolf, EntityAIWolfPack pack, EntityWolf rival, WolfPackConfig cfg, long now) {
        pack.endDuel(now + cfg.rivalryCooldownSeconds * 20L);
        // the fight back AI (hurt by the rival) would keep attacking it, restart it without a target
        restartTargetTasks(wolf, action -> action instanceof EntityAIHurtByTarget);
        if (wolf.getRevengeTarget() == rival) wolf.setRevengeTarget(null);
        if (wolf.getAttackTarget() == rival) wolf.setAttackTarget(null);
    }

    /** The loser's pack members join the winner as far as "Max Pack Size" allows, the rest go on their own */
    private static void transferPack(EntityWolf loser, EntityWolf winner, WolfPackConfig cfg) {
        int size = countPack(winner);
        for (EntityWolf member : getPackMembers(loser)) {
            if (size < cfg.maxPackSize) {
                setPackAlpha(member, winner);
                size++;
            } else {
                setPackAlpha(member, null);
            }
        }
    }

    /** Attacking something other than a player, or hurt by something other than a wolf recently */
    private static boolean isBusyFighting(EntityWolf wolf) {
        EntityLivingBase target = wolf.getAttackTarget();
        if (target != null && !(target instanceof EntityPlayer)) return true;
        EntityLivingBase revenge = wolf.getRevengeTarget();
        return revenge != null && !(revenge instanceof EntityWolf);
    }

    /** Alpha in a duel against someone else than the attacker, or fleeing after a lost duel: other wolves stay off it */
    private static boolean isProtectedFrom(EntityWolf alpha, EntityWolf attacker) {
        EntityAIWolfPack pack = EntityAIWolfPack.of(alpha);
        if (pack == null) return false;
        EntityWolf rival = pack.getRival();
        if (rival != null) return rival != attacker;
        return pack.isFleeing();
    }

    // ---------- pack membership ----------

    @Nullable
    public static UUID getPackAlphaId(EntityWolf wolf) {
        NBTTagCompound data = wolf.getEntityData();
        return data.hasUniqueId(PACK_ALPHA_KEY) ? data.getUniqueId(PACK_ALPHA_KEY) : null;
    }

    public static void setPackAlpha(EntityWolf wolf, @Nullable EntityWolf alpha) {
        NBTTagCompound data = wolf.getEntityData();
        data.removeTag(PACK_ALPHA_KEY + "Most");
        data.removeTag(PACK_ALPHA_KEY + "Least");
        if (alpha != null) data.setUniqueId(PACK_ALPHA_KEY, alpha.getUniqueID());
    }

    /** Loaded, living, untamed alpha in the same world as the wolf */
    public static boolean isValidAlpha(EntityWolf alpha, EntityWolf wolf) {
        return alpha.isEntityAlive() && alpha.world == wolf.world && !alpha.isTamed() && isAlpha(alpha);
    }

    /**
     * The wolf's pack alpha. A wolf whose alpha is gone (killed, or not loaded) leaves the pack, and a wolf without a pack
     * joins an alpha with room within "Pack Radius". Alphas, tamed wolves and babies have no pack
     */
    @Nullable
    public static EntityWolf updatePackMembership(EntityWolf wolf) {
        if (!(wolf.world instanceof WorldServer)) return null;
        if (isAlpha(wolf) || wolf.isTamed() || wolf.isChild()) {
            if (getPackAlphaId(wolf) != null) setPackAlpha(wolf, null);
            return null;
        }
        UUID id = getPackAlphaId(wolf);
        if (id != null) {
            Entity entity = ((WorldServer) wolf.world).getEntityFromUuid(id);
            if (entity instanceof EntityWolf && isValidAlpha((EntityWolf) entity, wolf)) return (EntityWolf) entity;
            WolfPackDebug.log(wolf, "leaves its pack: alpha {} gone ({})", id.toString().substring(0, 8), WolfPackDebug.describe(entity));
            setPackAlpha(wolf, null);
        }
        EntityWolf alpha = findJoinableAlpha(wolf, ForgeConfigHandler.wolfpack.packRadius);
        if (alpha != null) {
            WolfPackDebug.log(wolf, "joins pack of {}", WolfPackDebug.describe(alpha));
            setPackAlpha(wolf, alpha);
        }
        return alpha;
    }

    /** Nearest alpha within radius whose pack has room */
    @Nullable
    public static EntityWolf findJoinableAlpha(EntityWolf wolf, double radius) {
        List<EntityWolf> alphas = wolf.world.getEntitiesWithinAABB(EntityWolf.class, wolf.getEntityBoundingBox().grow(radius),
                other -> other != null && other != wolf && isValidAlpha(other, wolf));
        alphas.sort(Comparator.comparingDouble(wolf::getDistanceSq));
        int maxSize = ForgeConfigHandler.wolfpack.maxPackSize;
        for (EntityWolf alpha : alphas) {
            if (wolf.getDistanceSq(alpha) > radius * radius) break;
            if (countPack(alpha) < maxSize) return alpha;
        }
        return null;
    }

    /** Loaded pack members around the alpha */
    public static List<EntityWolf> getPackMembers(EntityWolf alpha) {
        UUID id = alpha.getUniqueID();
        return alpha.world.getEntitiesWithinAABB(EntityWolf.class, alpha.getEntityBoundingBox().grow(PACK_SCAN_RADIUS),
                other -> other != null && other != alpha && other.isEntityAlive() && !other.isTamed() && id.equals(getPackAlphaId(other)));
    }

    /** Pack size, the alpha included */
    public static int countPack(EntityWolf alpha) {
        return getPackMembers(alpha).size() + 1;
    }

    // ---------- helpers ----------

    /** "Alpha Name" for untamed, unnamed alphas, null for every other wolf (keeps its normal name) */
    @Nullable
    public static String getAlphaName(EntityWolf wolf) {
        WolfPackConfig cfg = ForgeConfigHandler.wolfpack;
        if (!cfg.enabled || cfg.alphaName.isEmpty() || wolf.hasCustomName() || !isAlpha(wolf)) return null;
        return cfg.alphaName;
    }

    /** Angry, attacking something or stalking a player (client side only angry/attacking are known) */
    public static boolean isHunting(EntityWolf wolf) {
        if (wolf.isAngry() || wolf.getAttackTarget() != null) return true;
        if (wolf.world.isRemote) return false;
        EntityAIWolfPack pack = EntityAIWolfPack.of(wolf);
        return pack != null && (pack.getStalkTarget() != null || pack.getRetrieveTarget() != null);
    }

    @Nullable
    public static EntityWolf findNearestAlpha(EntityWolf wolf, double radius) {
        List<EntityWolf> alphas = wolf.world.getEntitiesWithinAABB(EntityWolf.class, wolf.getEntityBoundingBox().grow(radius),
                other -> other != null && other != wolf && other.isEntityAlive() && !other.isTamed() && isAlpha(other));
        EntityWolf nearest = null;
        double best = radius * radius;
        for (EntityWolf alpha : alphas) {
            double distSq = wolf.getDistanceSq(alpha);
            if (distSq <= best) {
                best = distSq;
                nearest = alpha;
            }
        }
        return nearest;
    }

    /** "Hunt Condition": 0 always, 1 night, 2 cold (Simple Difficulty), 3 night or cold */
    public static boolean isHuntingWeather(EntityWolf wolf) {
        int condition = ForgeConfigHandler.wolfpack.huntCondition;
        if (condition <= 0) return true;
        if ((condition == 1 || condition >= 3) && isNight(wolf)) return true;
        return (condition == 2 || condition >= 3) && isCold(wolf);
    }

    private static boolean isNight(EntityWolf wolf) {
        return wolf.world.getWorldTime() % 24000L >= 12000L;
    }

    private static boolean isCold(EntityWolf wolf) {
        if (!ModLoadedUtil.simpledifficulty.isLoaded()) return false;
        Integer temperature = SimpleDifficultyTemperature.getWorldTemperature(wolf.world, new BlockPos(wolf));
        return temperature != null && temperature <= ForgeConfigHandler.wolfpack.coldTemperature;
    }

    public static boolean isHoldingCalmingItem(EntityPlayer player) {
        return isCalmingItem(player.getHeldItemMainhand()) || isCalmingItem(player.getHeldItemOffhand());
    }

    private static boolean isCalmingItem(ItemStack stack) {
        String id = getItemId(stack);
        return id != null && findCalmingEntry(id) != null;
    }

    /** "Calming Items" entry (itemid,min,max) of this item, split and trimmed, or null */
    @Nullable
    private static String[] findCalmingEntry(String id) {
        for (String entry : ForgeConfigHandler.wolfpack.calmingItems) {
            String[] parts = entry.split(",");
            for (int i = 0; i < parts.length; i++) parts[i] = parts[i].trim();
            if (parts[0].equals(id)) return parts;
        }
        return null;
    }

    /** Min (part 1) or max (part 2) amount of a calming item, 1 when not set or invalid */
    private static int getAmount(String id, int part) {
        String[] entry = findCalmingEntry(id);
        if (entry == null) return 1;
        String value = entry.length > part ? entry[part] : entry.length > 1 ? entry[1] : null; // only a min = exactly that many
        if (value == null) return 1;
        try {
            return Math.max(1, Integer.parseInt(value));
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    @Nullable
    private static String getItemId(ItemStack stack) {
        if (stack.isEmpty()) return null;
        ResourceLocation id = stack.getItem().getRegistryName();
        return id != null ? id.toString() : null;
    }

    /** The calming items that exist in this game (mods that aren't installed are skipped): giving all of them tames an alpha */
    private static Set<String> getTameItems() {
        Set<String> items = new HashSet<>();
        for (String entry : ForgeConfigHandler.wolfpack.calmingItems) {
            String id = entry.split(",")[0].trim();
            if (!id.isEmpty() && ForgeRegistries.ITEMS.containsKey(new ResourceLocation(id))) items.add(id);
        }
        return items;
    }

    /** (Re)applies the health modifier so config changes reach existing alphas, and removes them from non-alphas */
    private static void applyAlphaStats(EntityWolf wolf, boolean heal) {
        WolfPackConfig cfg = ForgeConfigHandler.wolfpack;
        boolean alpha = isAlpha(wolf);
        setModifier(wolf, SharedMonsterAttributes.MAX_HEALTH, HEALTH_UUID, "EagleMixins alpha wolf health", alpha ? cfg.alphaHealthMultiplier : 1.0D);
        setModifier(wolf, SharedMonsterAttributes.MOVEMENT_SPEED, OLD_ALPHA_SPEED_UUID, "", 1.0D); // removes the old alpha speed
        setFollowRange(wolf, alpha ? cfg.alphaFollowRange : (!wolf.isTamed() ? cfg.memberFollowRange : -1.0D));
        if (heal) wolf.setHealth(wolf.getMaxHealth());
        else if (wolf.getHealth() > wolf.getMaxHealth()) wolf.setHealth(wolf.getMaxHealth());
    }

    private static void setModifier(EntityWolf wolf, IAttribute attribute, UUID uuid, String name, double multiplier) {
        IAttributeInstance instance = wolf.getEntityAttribute(attribute);
        if (instance == null) return;
        if (instance.getModifier(uuid) != null) instance.removeModifier(uuid);
        if (multiplier != 1.0D) instance.applyModifier(new AttributeModifier(uuid, name, multiplier - 1.0D, 2));
    }
}
