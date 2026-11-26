package eaglemixins.handlers;

import eaglemixins.EagleMixins;
import eaglemixins.config.ForgeConfigHandler;
import eaglemixins.entity.ai.EntityAIWolfPack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.passive.EntityWolf;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Debug log for wolf packs ("Debug Logging" in the wolf pack config). Every line starts with [WolfPack] and the wolf:
 * its role (Alpha / Member of alpha #id / Loner / Tamed), entity id, position and angry state
 */
public final class WolfPackDebug {
    /** Ticks between state dumps of angry / targeting / stalking wolves */
    public static final int DUMP_INTERVAL = 100;
    /** Stack frames of these classes are skipped when looking for who set an attack target */
    private static final String[] SKIP_FRAMES = {
            "java.", "sun.", "jdk.", "eaglemixins.handlers.WolfPackDebug", "net.minecraftforge.common.ForgeHooks",
            "net.minecraftforge.fml.common.eventhandler.", "net.minecraftforge.event.", "net.minecraft.entity.EntityLiving.",
            "net.minecraft.entity.passive.EntityWolf.", "net.minecraft.entity.EntityLiving$", "net.minecraft.entity.passive.EntityWolf$"
    };

    private WolfPackDebug() {
    }

    public static boolean on() {
        return ForgeConfigHandler.wolfpack.debugLogging;
    }

    /** Logs "[WolfPack] <wolf> <message>", message uses {} placeholders like log4j */
    public static void log(EntityWolf wolf, String message, Object... args) {
        if (!on() || wolf.world.isRemote) return;
        EagleMixins.LOGGER.info("[WolfPack] " + describe(wolf) + " " + message, args);
    }

    public static String describe(@Nullable Entity entity) {
        if (entity == null) return "none";
        if (!(entity instanceof EntityWolf)) {
            return entity.getName() + "#" + entity.getEntityId() + (entity.isEntityAlive() ? "" : "(DEAD)");
        }
        EntityWolf wolf = (EntityWolf) entity;
        String role;
        if (wolf.isTamed()) role = WolfPackHandler.isAlpha(wolf) ? "TamedAlpha" : "Tamed";
        else if (WolfPackHandler.isAlpha(wolf)) role = "Alpha";
        else {
            UUID alphaId = WolfPackHandler.getPackAlphaId(wolf);
            role = alphaId == null ? "Loner" : "Member(alpha " + shortId(alphaId) + ")";
        }
        return String.format("%s#%d[%s @%d,%d,%d%s]", role, wolf.getEntityId(), shortId(wolf.getUniqueID()),
                (int) Math.floor(wolf.posX), (int) Math.floor(wolf.posY), (int) Math.floor(wolf.posZ), wolf.isAngry() ? " ANGRY" : "");
    }

    private static String shortId(UUID id) {
        return id.toString().substring(0, 8);
    }

    /** The first stack frame outside the attack target plumbing, so the log shows which AI / code set it */
    public static String caller() {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        List<String> frames = new ArrayList<>();
        for (StackTraceElement frame : stack) {
            String name = frame.getClassName() + "." + frame.getMethodName();
            boolean skip = false;
            for (String prefix : SKIP_FRAMES) {
                if (name.startsWith(prefix)) {
                    skip = true;
                    break;
                }
            }
            if (skip) continue;
            String cls = frame.getClassName();
            frames.add(cls.substring(cls.lastIndexOf('.') + 1) + "." + frame.getMethodName() + ":" + frame.getLineNumber());
            if (frames.size() >= 3) break;
        }
        return String.join(" <- ", frames);
    }

    /** Full state of a wolf that's angry, has a target or is stalking: targets, pack state and running AI tasks */
    public static void dump(EntityWolf wolf) {
        if (!on()) return;
        EntityLivingBase target = wolf.getAttackTarget();
        EntityLivingBase revenge = wolf.getRevengeTarget();
        EntityAIWolfPack pack = EntityAIWolfPack.of(wolf);
        String packState = pack == null ? "no pack AI" : pack.debugState();
        log(wolf, "STATE target={} (dist {}) revenge={} (timer {} ago) | {} | target tasks: {} | tasks: {}",
                describe(target), target != null ? String.format("%.1f", wolf.getDistance(target)) : "-",
                describe(revenge), wolf.ticksExisted - wolf.getRevengeTimer(), packState,
                running(wolf.targetTasks), running(wolf.tasks));
    }

    private static String running(EntityAITasks tasks) {
        List<String> names = new ArrayList<>();
        for (EntityAITasks.EntityAITaskEntry entry : tasks.taskEntries) {
            if (entry.using) names.add(entry.priority + ":" + name(entry.action));
        }
        return names.isEmpty() ? "-" : String.join(", ", names);
    }

    private static String name(EntityAIBase action) {
        String name = action.getClass().getSimpleName();
        return name.isEmpty() ? action.getClass().getName() : name;
    }
}
