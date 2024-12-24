package eaglemixins.handlers;

import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.VillagerRegistry;

import java.util.Random;

public class BerianHandler {

    private static final ResourceLocation LIBRARIAN = new ResourceLocation("minecraft:librarian");

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (event.isCanceled() 
                || event.getWorld().isRemote
                || !(event.getEntity() instanceof EntityVillager)) {
            return;
        }
        if (ForgeConfigHandler.server.sussyberianChance > 0F || ForgeConfigHandler.server.mentalberianChance > 0F) {
            updateBerian((EntityVillager) event.getEntity(), event.getWorld().rand);
        }

    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onLivingSpawn(LivingSpawnEvent event) {
        if (event.isCanceled()
                || event.getWorld().isRemote
                || !(event.getEntity() instanceof EntityVillager)) {
            return;
        }
        if (ForgeConfigHandler.server.sussyberianChance > 0F || ForgeConfigHandler.server.mentalberianChance > 0F) {
            updateBerian((EntityVillager) event.getEntity(), event.getWorld().rand);
        }
    }

    private static void updateBerian(EntityVillager villager, Random rand) {
        VillagerRegistry.VillagerProfession profession = villager.getProfessionForge();
        if (LIBRARIAN.equals(profession.getRegistryName())) {
            if (villager.getEntityData().getTag("SussyBerianNaming") == null) {
                //give entity a tag to make sure this script only iterates once per entity.
                villager.getEntityData().setString("SussyBerianNaming", String.valueOf(1));

                if (rand.nextFloat() < ForgeConfigHandler.server.sussyberianChance) {
                    villager.setCustomNameTag("Sussyberian");
                } else if (rand.nextFloat() < ForgeConfigHandler.server.mentalberianChance) {
                    villager.setCustomNameTag("Mentalberian");
                }
            }
        }
    }
}
