package eaglemixins.handlers;

import eaglemixins.config.ForgeConfigHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.VillagerRegistry;

import java.util.List;

public class BerianHandler {

    private static final ResourceLocation LIBRARIAN = new ResourceLocation("minecraft:librarian");

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onEntityJoinWorld(EntityJoinWorldEvent event) {
        updateBerian(event.getEntity());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingSpawn(LivingSpawnEvent.CheckSpawn event) {
        updateBerian(event.getEntity());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        if(event.getEntity().ticksExisted == 1)
            updateBerian(event.getEntity());
    }

    private static void updateBerian(Entity entity) {
        if (entity.world.isRemote) return;
        if (!(entity instanceof EntityVillager)) return;
        if (ForgeConfigHandler.berian.sussyberianChance <= 0.0D && ForgeConfigHandler.berian.mentalberianChance <= 0.0D) return;

        EntityVillager villager = (EntityVillager) entity;
        VillagerRegistry.VillagerProfession profession = villager.getProfessionForge();
        if (!LIBRARIAN.equals(profession.getRegistryName())) return;

        //If they are already named (special villagers), don't make them berians
        if (villager.hasCustomName()) return;

        //Old check using String instead of boolean
        if (villager.getEntityData().hasKey("SussyBerianNaming")) return;
        //New check
        if (villager.getEntityData().hasKey("BerianCheck")) return;
        villager.getEntityData().setBoolean("BerianCheck", true);

        //First check whether it should be any berian, adding the two chances together
        // --> 5%+5%=10% of all librarians are berian
        double combinedChance = ForgeConfigHandler.berian.sussyberianChance + ForgeConfigHandler.berian.mentalberianChance;
        if(villager.getRNG().nextFloat() > combinedChance) return;

        //Then the sussy vs mental roll can be independent, either it's a sussy or a mental
        // --> random float between 0 and sussy+mental smaller than sussy
        if(villager.getRNG().nextFloat() * combinedChance < ForgeConfigHandler.berian.sussyberianChance){
            villager.setCustomNameTag("Sussyberian");
            villager.getEntityData().setBoolean("Sussyberian", true);
        } else {
            villager.setCustomNameTag("Mentalberian");
            villager.getEntityData().setBoolean("Mentalberian", true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onPlayerInteractEntity(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof EntityVillager)) return;

        EntityVillager villager = (EntityVillager) event.getTarget();
        boolean isMental = villager.getEntityData().getBoolean("Mentalberian");
        boolean isSussy = villager.getEntityData().getBoolean("Sussyberian");
        if (isMental || isSussy) {
            applyBerianEffects(event.getEntityPlayer(), villager, isSussy);
            event.setCanceled(true);
        }
    }

    private static void applyBerianEffects(EntityPlayer player, EntityVillager berian, boolean isSussy) {
        List<Potion> potions = isSussy ? ForgeConfigHandler.berian.getSussyberianEffects() : ForgeConfigHandler.berian.getMentalberianEffects();
        if (!potions.isEmpty()) {
            //Random potion
            Potion potion = potions.get(player.getRNG().nextInt(potions.size()));
            if (potion.isInstant())
                potion.affectEntity(berian, berian, player, 2, 1.0D);
            else
                player.addPotionEffect(new PotionEffect(potion, 200, 2));
        }
        //Always apply this potion
        Potion potion = ForgeConfigHandler.berian.getBerianConstantEffect();
        if (potion.isInstant())
            potion.affectEntity(berian, berian, player, 1, 1.0D);
        else
            player.addPotionEffect(new PotionEffect(potion, 200, 1));
    }
}
