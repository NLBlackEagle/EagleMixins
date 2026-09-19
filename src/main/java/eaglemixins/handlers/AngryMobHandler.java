package eaglemixins.handlers;

import eaglemixins.config.ForgeConfigHandler;
import eaglemixins.config.folders.AngryMobConfig;
import eaglemixins.entity.ai.EntityAIAngryMob;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Attaches {@link EntityAIAngryMob} to every untamed-or-tamed eligible mob as it enters the world
 * (spawn and every world load). The AI itself stays dormant unless the "Angry Mobs" config
 * enables it, so this is cheap to always apply.
 */
public class AngryMobHandler {

    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (event.getWorld().isRemote) return;
        Entity entity = event.getEntity();
        if (!(entity instanceof EntityCreature)) return;
        EntityCreature creature = (EntityCreature) entity;

        ResourceLocation loc = EntityList.getKey(entity);
        if(loc == null) return; //players mainly

        AngryMobConfig.Entry cfgEntry = ForgeConfigHandler.angrymobs.angryMobs.get(loc);
        if (cfgEntry == null) return;

        if(creature.targetTasks.taskEntries.stream().anyMatch(t -> t.action instanceof EntityAIAngryMob)) return;
        creature.targetTasks.addTask(cfgEntry.aiPriority, new EntityAIAngryMob(creature, cfgEntry));
    }
}
