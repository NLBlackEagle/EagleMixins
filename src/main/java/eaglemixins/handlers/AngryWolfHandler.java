package eaglemixins.handlers;

import eaglemixins.entity.ai.EntityAIAngryWolf;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Attaches {@link EntityAIAngryWolf} to every untamed-or-tamed wolf as it enters the world
 * (spawn and every world load). The AI itself stays dormant unless the "Angry Mobs" config
 * enables it, so this is cheap to always apply.
 */
public class AngryWolfHandler {

    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (event.getWorld().isRemote) return;
        if (!(event.getEntity() instanceof EntityWolf)) return;

        EntityWolf wolf = (EntityWolf) event.getEntity();
        for (EntityAITasks.EntityAITaskEntry task : wolf.targetTasks.taskEntries) {
            if (task.action instanceof EntityAIAngryWolf) return;
        }
        wolf.targetTasks.addTask(4, new EntityAIAngryWolf(wolf));
    }
}
