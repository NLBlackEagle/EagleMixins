package eaglemixins.handlers;

import eaglemixins.config.ForgeConfigHandler;
import eaglemixins.config.folders.LeapConfig;
import eaglemixins.entity.ai.EntityAILeap;
import eaglemixins.entity.ai.LeapSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAILeapAtTarget;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Attaches {@link EntityAILeap} to mobs as they join the world (spawn and every load), based on
 * the "Leaping Mobs" config (by entity id, then by ForgeData flag) and the ForgeData.EagleMixinsLeap tag.
 */
public class LeapHandler {
    //Summon NBT: {ForgeData:{EagleMixinsLeap:1b}} or {ForgeData:{EagleMixinsLeap:{maxRange:12d}}}
    public static final String NBT_KEY = "EagleMixinsLeap";
    private static final int RECHECK_TICKS = 20;
    //Mobs that didn't match on join, rechecked against the ForgeData flag rules for RECHECK_TICKS ticks
    private static final Map<EntityLiving, Integer> PENDING = new WeakHashMap<>();

    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (event.getWorld().isRemote) return;
        if (!ForgeConfigHandler.leap.enabled) return;
        Entity entity = event.getEntity();
        if (!(entity instanceof EntityLiving)) return;
        EntityLiving mob = (EntityLiving) entity;

        if (hasLeap(mob)) return;
        tryAttach(mob);
        //Some mods (e.g. Scaling Health blights) set their ForgeData flag a few ticks after joining, so recheck for a bit.
        //Also for mobs that already got an entity id rule, since a flag rule wins over it (a blight zombie leaps like a blight)
        if (!ForgeConfigHandler.leap.getByFlag().isEmpty() && matchingFlag(mob) == null) PENDING.put(mob, RECHECK_TICKS);
    }

    @SubscribeEvent
    public static void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        if (PENDING.isEmpty()) return;
        EntityLivingBase entity = event.getEntityLiving();
        if (entity.world.isRemote) return;
        Integer ticksLeft = PENDING.get(entity);
        if (ticksLeft == null) return;

        EntityLiving mob = (EntityLiving) entity;
        if (matchingFlag(mob) != null) {
            removeLeap(mob); //replace an entity id rule leap with the flag rule
            tryAttach(mob);
            PENDING.remove(mob);
        } else if (ticksLeft <= 1) PENDING.remove(mob);
        else PENDING.put(mob, ticksLeft - 1);
    }

    /** Knocked back while flying a leap's arch: batted away on a homerun arch instead (EntityAILeap#onKnockback) */
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onKnockBack(LivingKnockBackEvent event) {
        if (!(event.getEntityLiving() instanceof EntityLiving)) return;
        EntityLiving mob = (EntityLiving) event.getEntityLiving();
        if (mob.world.isRemote) return;
        for (EntityAITasks.EntityAITaskEntry task : mob.tasks.taskEntries) {
            if (task.action instanceof EntityAILeap) {
                if (((EntityAILeap) task.action).onKnockback(event.getStrength(), event.getRatioX(), event.getRatioZ()))
                    event.setCanceled(true);
                return;
            }
        }
    }

    private static boolean hasLeap(EntityLiving mob) {
        return mob.tasks.taskEntries.stream().anyMatch(t -> t.action instanceof EntityAILeap);
    }

    private static void removeLeap(EntityLiving mob) {
        List<EntityAIBase> leaps = new ArrayList<>();
        for (EntityAITasks.EntityAITaskEntry task : mob.tasks.taskEntries) if (task.action instanceof EntityAILeap) leaps.add(task.action);
        for (EntityAIBase leap : leaps) mob.tasks.removeTask(leap);
    }

    /** The first flag rule whose ForgeData flag the mob has */
    @Nullable
    private static LeapSettings matchingFlag(EntityLiving mob) {
        NBTTagCompound forgeData = mob.getEntityData();
        for (Map.Entry<String, LeapSettings> flagRule : ForgeConfigHandler.leap.getByFlag().entrySet()) {
            if (forgeData.getBoolean(flagRule.getKey())) return flagRule.getValue();
        }
        return null;
    }

    /**
     * API for other mods: checks a mob that's already in the world again, for when they add the EagleMixinsLeap tag
     * (or a flag from Leap Rules By ForgeData Flag) after it joined. True if the mob leaps now (or already did).
     */
    public static boolean recheck(EntityLiving mob) {
        if (mob.world.isRemote || !ForgeConfigHandler.leap.enabled) return false;
        if (hasLeap(mob)) return true;
        return tryAttach(mob);
    }

    private static boolean tryAttach(EntityLiving mob) {
        LeapSettings settings = resolve(mob);
        if (settings == null) return false;
        if (settings.removeVanillaLeap) mob.tasks.taskEntries.removeIf(t -> t.action instanceof EntityAILeapAtTarget);
        mob.tasks.addTask(leapPriority(mob, settings.aiPriority), new EntityAILeap(mob, settings));
        return true;
    }

    /**
     * A task can't start while a more important (lower priority) task that uses the same controls runs. The melee attack AI
     * runs the whole time a mob chases its target, so a leap behind it (zombies attack at 2) only ever fired when the mob
     * couldn't path to its target. The leap goes in front of the melee attack AI instead, the attack picks up again after landing.
     */
    private static int leapPriority(EntityLiving mob, int configured) {
        int priority = configured;
        for (EntityAITasks.EntityAITaskEntry task : mob.tasks.taskEntries) {
            if (isAttackTask(task.action) && task.priority <= priority) priority = Math.max(0, task.priority - 1);
        }
        return priority;
    }

    /** Vanilla melee attacks, and modded ones that don't extend them (Epic Siege Mod's ESM_EntityAIZombieAttack, Lycanites' attack goals) */
    private static boolean isAttackTask(EntityAIBase action) {
        if (action instanceof EntityAIAttackMelee) return true;
        if (action instanceof EntityAILeap) return false;
        return action.getClass().getSimpleName().contains("Attack");
    }

    @Nullable
    private static LeapSettings resolve(EntityLiving mob) {
        LeapConfig cfg = ForgeConfigHandler.leap;
        NBTTagCompound forgeData = mob.getEntityData();

        //Flags first: they mark special mobs (blights), which should leap like that and not like their regular species
        LeapSettings rule = matchingFlag(mob);
        if (rule == null) {
            ResourceLocation id = EntityList.getKey(mob);
            if (id != null) rule = cfg.getByEntity().get(id);
        }

        //Settings objects are only read by the AI, so config ones are shared and only NBT-modified ones are copied
        if (forgeData.hasKey(NBT_KEY, Constants.NBT.TAG_COMPOUND)) {
            return (rule != null ? rule : cfg.getDefaults()).copy().apply(forgeData.getCompoundTag(NBT_KEY));
        }
        if (forgeData.hasKey(NBT_KEY, Constants.NBT.TAG_ANY_NUMERIC)) {
            if (!forgeData.getBoolean(NBT_KEY)) return null; //EagleMixinsLeap:0b explicitly disables
            return rule != null ? rule : cfg.getDefaults();
        }
        return rule;
    }
}
