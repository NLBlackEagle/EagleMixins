package eaglemixins.handlers;

import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
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
        if(event.getWorld().isRemote) return;
        if(!(event.getEntity() instanceof EntityVillager)) return;
        
        if(ForgeConfigHandler.server.sussyberianChance > 0.0D || ForgeConfigHandler.server.mentalberianChance > 0.0D) {
            updateBerian((EntityVillager)event.getEntity());
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingSpawn(LivingSpawnEvent event) {
        if(event.getWorld().isRemote) return;
        if(!(event.getEntity() instanceof EntityVillager)) return;
        
        if(ForgeConfigHandler.server.sussyberianChance > 0.0D || ForgeConfigHandler.server.mentalberianChance > 0.0D) {
            updateBerian((EntityVillager)event.getEntity());
        }
    }
    
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onPlayerInteractEntity(PlayerInteractEvent.EntityInteract event) {
        if(event.getWorld().isRemote) return;
        if(!(event.getTarget() instanceof EntityVillager)) return;
        
        EntityVillager villager = (EntityVillager)event.getTarget();
        if(villager.getEntityData().getBoolean("Sussyberian")) {
            List<Potion> potions = ForgeConfigHandler.getSussyberianEffects();
            if(!potions.isEmpty()) {
                Potion potion = potions.get(event.getWorld().rand.nextInt(potions.size()));
                if(potion.isInstant()) potion.affectEntity(villager, villager, event.getEntityPlayer(), 2, 1.0D);
                else event.getEntityPlayer().addPotionEffect(new PotionEffect(potion, 200, 2));
            }
            Potion potion = ForgeConfigHandler.getBerianConstantEffect();
            if(potion.isInstant()) potion.affectEntity(villager, villager, event.getEntityPlayer(), 1, 1.0D);
            else event.getEntityPlayer().addPotionEffect(new PotionEffect(potion, 200, 1));
        }
        else if(villager.getEntityData().getBoolean("Mentalberian")) {
            List<Potion> potions = ForgeConfigHandler.getMentalberianEffects();
            if(!potions.isEmpty()) {
                Potion potion = potions.get(event.getWorld().rand.nextInt(potions.size()));
                if(potion.isInstant()) potion.affectEntity(villager, villager, event.getEntityPlayer(), 1, 1.0D);
                else event.getEntityPlayer().addPotionEffect(new PotionEffect(potion, 200, 1));
            }
            Potion potion = ForgeConfigHandler.getBerianConstantEffect();
            if(potion.isInstant()) potion.affectEntity(villager, villager, event.getEntityPlayer(), 1, 1.0D);
            else event.getEntityPlayer().addPotionEffect(new PotionEffect(potion, 200, 1));
        }
    }

    private static void updateBerian(EntityVillager villager) {
        VillagerRegistry.VillagerProfession profession = villager.getProfessionForge();
        if(LIBRARIAN.equals(profession.getRegistryName())) {
            if(villager.getEntityData().getString("SussyBerianNaming").isEmpty()) {
                //Should just be a boolean but previous worlds already used String
                villager.getEntityData().setString("SussyBerianNaming", "true");
                
                if(villager.getRNG().nextFloat() < ForgeConfigHandler.server.sussyberianChance) {
                    villager.setCustomNameTag("Sussyberian");
                    villager.getEntityData().setBoolean("Sussyberian", true);
                }
                else if(villager.getRNG().nextFloat() < ForgeConfigHandler.server.mentalberianChance) {
                    villager.setCustomNameTag("Mentalberian");
                    villager.getEntityData().setBoolean("Mentalberian", true);
                }
            }
        }
    }
}