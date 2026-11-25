package eaglemixins.handlers;

import eaglemixins.compat.ModLoadedUtil;
import eaglemixins.compat.SimpleDifficultyTemperature;
import eaglemixins.config.ForgeConfigHandler;
import eaglemixins.config.folders.WolfPackConfig;
import eaglemixins.entity.ai.EntityAIPackGate;
import eaglemixins.entity.ai.EntityAIWolfFlee;
import eaglemixins.entity.ai.EntityAIWolfFollowAlpha;
import eaglemixins.entity.ai.EntityAIWolfPack;
import eaglemixins.entity.ai.EntityAIWolfStalk;
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
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.UUID;
import java.util.WeakHashMap;

/**
 * Wolf packs: every wild pack gets a fixed alpha (entity tag, saved with the wolf). The alpha is stronger, untamable,
 * has its own skin, and decides when the pack hunts players (see {@link EntityAIWolfPack}).
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
            } else {
                setPackAlpha(wolf, findJoinableAlpha(wolf, cfg.alphaSearchRadius));
            }
        }
        if (isAlpha(wolf) && wolf.isTamed()) wolf.removeTag(ALPHA_TAG); // tamed by other means (commands)
        if (isAlpha(wolf) || wolf.isTamed()) setPackAlpha(wolf, null);
        wolf.removeTag(STALKING_TAG); // saved mid-stalk

        applyAlphaStats(wolf, newAlpha);

        if (wolf.targetTasks.taskEntries.stream().noneMatch(t -> t.action instanceof EntityAIWolfPack)) {
            EntityAIWolfPack pack = new EntityAIWolfPack(wolf);
            wolf.targetTasks.addTask(cfg.aiPriority, pack);
            wolf.tasks.addTask(STALK_PRIORITY, new EntityAIWolfStalk(wolf, pack));
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
        if (inPack && wolf.isAngry() && wolf.getAttackTarget() == null) wolf.setAngry(false); // angry without a target
        if (packAlpha != null) copyAlpha(wolf, packAlpha);
        else if (alpha) dropOutOfRangeTarget(wolf);
        if (cfg.enabled && isAlpha(wolf) && !wolf.isTamed()) {
            EntityAIWolfPack pack = EntityAIWolfPack.of(wolf);
            if (pack != null) {
                if (wolf.getAttackTarget() instanceof EntityPlayer && cfg.rememberTargetSeconds > 0) {
                    pack.rememberTarget((EntityPlayer) wolf.getAttackTarget(), wolf.world.getTotalWorldTime() + cfg.rememberTargetSeconds * 20L);
                }
                if (pack.lostPlayerAggro(wolf.getAttackTarget() instanceof EntityPlayer)) {
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

    /** Hunting wild wolves can't be fed into love mode (Wolf Armor and Storage lets wild wolves breed), and alphas can't be tamed */
    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!ForgeConfigHandler.wolfpack.enabled) return;
        Entity target = event.getTarget();
        if (!isWolf(target)) return;
        EntityWolf wolf = (EntityWolf) target;
        if (wolf.isTamed()) return;

        if (wolf.isBreedingItem(event.getItemStack()) && isHunting(wolf)) {
            event.setCanceled(true);
            event.setCancellationResult(EnumActionResult.FAIL);
            return;
        }

        if (!isAlpha(wolf) || event.getItemStack().getItem() != Items.BONE) return;

        event.setCanceled(true);
        event.setCancellationResult(EnumActionResult.SUCCESS);
        if (!wolf.world.isRemote && event.getHand() == EnumHand.MAIN_HAND) {
            wolf.world.setEntityState(wolf, (byte) 6); // failed taming smoke
        }
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
        if (lost) clearTarget(wolf);
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
                alpha.setAttackTarget(revenge);
                alphaTarget = revenge;
            }
        }

        EntityLivingBase current = wolf.getAttackTarget();
        IAttributeInstance followRange = wolf.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE);
        double range = followRange != null ? followRange.getAttributeValue() : 16.0D;
        if (alphaTarget != null && alphaTarget.isEntityAlive() && alphaTarget.world == wolf.world
                && wolf.getDistanceSq(alphaTarget) <= range * range) {
            if (current != alphaTarget) wolf.setAttackTarget(alphaTarget);
        } else if (current != null) {
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
            endDuel(wolf, pack, rival, cfg, now);
            if (rivalPack != null && rivalPack.getRival() == wolf) endDuel(rival, rivalPack, wolf, cfg, now);
            return;
        }

        if (wolf.getHealth() > wolf.getMaxHealth() * cfg.rivalryFleeHealth) return;
        // lost: flee from the winner, the pack goes over to the winner
        endDuel(rival, rivalPack, wolf, cfg, now);
        endDuel(wolf, pack, rival, cfg, now);
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
            setPackAlpha(wolf, null);
        }
        EntityWolf alpha = findJoinableAlpha(wolf, ForgeConfigHandler.wolfpack.packRadius);
        if (alpha != null) setPackAlpha(wolf, alpha);
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
        if (!cfg.enabled || cfg.alphaName.isEmpty() || wolf.isTamed() || wolf.hasCustomName() || !isAlpha(wolf)) return null;
        return cfg.alphaName;
    }

    /** Angry, attacking something or stalking a player (client side only angry/attacking are known) */
    public static boolean isHunting(EntityWolf wolf) {
        if (wolf.isAngry() || wolf.getAttackTarget() != null) return true;
        if (wolf.world.isRemote) return false;
        EntityAIWolfPack pack = EntityAIWolfPack.of(wolf);
        return pack != null && pack.getStalkTarget() != null;
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
        if (stack.isEmpty()) return false;
        ResourceLocation id = stack.getItem().getRegistryName();
        if (id == null) return false;
        String idString = id.toString();
        for (String calming : ForgeConfigHandler.wolfpack.calmingItems) {
            if (idString.equals(calming.trim())) return true;
        }
        return false;
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
