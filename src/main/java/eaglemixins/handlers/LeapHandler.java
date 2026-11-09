package eaglemixins.handlers;

import eaglemixins.config.ForgeConfigHandler;
import eaglemixins.config.folders.LeapConfig;
import eaglemixins.entity.ai.EntityAILeap;
import eaglemixins.entity.ai.LeapSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAILeapAtTarget;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nullable;
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

        if (mob.tasks.taskEntries.stream().anyMatch(t -> t.action instanceof EntityAILeap)) return;
        if (tryAttach(mob)) return;
        //Some mods (e.g. Scaling Health blights) set their ForgeData flag a few ticks after joining, so recheck for a bit
        if (!ForgeConfigHandler.leap.getByFlag().isEmpty()) PENDING.put(mob, RECHECK_TICKS);
    }

    @SubscribeEvent
    public static void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        if (PENDING.isEmpty()) return;
        EntityLivingBase entity = event.getEntityLiving();
        if (entity.world.isRemote) return;
        Integer ticksLeft = PENDING.get(entity);
        if (ticksLeft == null) return;

        EntityLiving mob = (EntityLiving) entity;
        if (tryAttach(mob) || ticksLeft <= 1) PENDING.remove(mob);
        else PENDING.put(mob, ticksLeft - 1);
    }

    private static boolean tryAttach(EntityLiving mob) {
        LeapSettings settings = resolve(mob);
        if (settings == null) return false;
        if (settings.removeVanillaLeap) mob.tasks.taskEntries.removeIf(t -> t.action instanceof EntityAILeapAtTarget);
        mob.tasks.addTask(settings.aiPriority, new EntityAILeap(mob, settings));
        return true;
    }

    @Nullable
    private static LeapSettings resolve(EntityLiving mob) {
        LeapConfig cfg = ForgeConfigHandler.leap;
        NBTTagCompound forgeData = mob.getEntityData();

        LeapSettings rule = null;
        ResourceLocation id = EntityList.getKey(mob);
        if (id != null) rule = cfg.getByEntity().get(id);
        if (rule == null) {
            for (Map.Entry<String, LeapSettings> flagRule : cfg.getByFlag().entrySet()) {
                if (forgeData.getBoolean(flagRule.getKey())) {
                    rule = flagRule.getValue();
                    break;
                }
            }
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
