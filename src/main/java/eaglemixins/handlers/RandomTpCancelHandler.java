package eaglemixins.handlers;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemChorusFruit;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.PotionEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.ArrayList;
import java.util.Arrays;

public class RandomTpCancelHandler {

    //Can't lazy load these as is, cause OTG has its own biome registry. Would have to import OTG as well, might do that at some point
    private static final ArrayList<String> biomeNames = new ArrayList<>(Arrays.asList("Parasite Biome", "Abyssal Rift"));

    //When eating chorus fruit
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onItemUseFinish(LivingEntityUseItemEvent.Finish event) {
        EntityLivingBase entityApplied = event.getEntityLiving();
        if(entityApplied.world.isRemote) return;
        if (!(entityApplied instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) event.getEntityLiving();
        if (player == null) return;
        if (!(event.getItem().getItem() instanceof ItemChorusFruit)) return;
        String biomeName = player.world.getBiome(player.getPosition()).getBiomeName();
        if (!biomeNames.contains(biomeName)) return;

        applyTpCooldownDebuffs(player);
    }

    //When applying a potion effect
    @SubscribeEvent
    public static void onPotionApplicable(PotionEvent.PotionAddedEvent event) {
        EntityLivingBase entityApplied = event.getEntityLiving();
        if(entityApplied.world.isRemote) return;
        if (!(entityApplied instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) entityApplied;
        if (!biomeNames.contains(entityApplied.world.getBiome(entityApplied.getPosition()).getBiomeName())) return;

        if (isTpPotion(event.getPotionEffect().getPotion()))
            applyTpCooldownDebuffs(player);
    }

    private static ArrayList<Potion> tpPotions = null;
    private static final ArrayList<String> tpPotionStrings = new ArrayList<>(Arrays.asList("potioncore:teleport", "potioncore:strong_teleport", "mujmajnkraftsbettersurvival:warp"));

    private static boolean isTpPotion(Potion potion) {
        if (tpPotions == null) {
            tpPotions = new ArrayList<>();
            for (String potionString : tpPotionStrings) {
                ResourceLocation location = new ResourceLocation(potionString);
                if (ForgeRegistries.POTIONS.containsKey(location))
                    tpPotions.add(ForgeRegistries.POTIONS.getValue(location));
            }
        }
        return tpPotions.contains(potion);
    }

    private static ArrayList<Potion> tpCooldownPotions = null;
    private static final ArrayList<String> tpCooldownPotionStrings = new ArrayList<>(Arrays.asList("potioncore:potion_sickness", "potioncore:teleport_surface"));

    private static void applyTpCooldownDebuffs(EntityPlayer player) {
        if (!player.getEntityData().hasKey("TeleportCooldown")) {
            player.getEntityData().setLong("TeleportCooldown", player.world.getTotalWorldTime());
        } else {
            long oldCooldown = player.getEntityData().getLong("TeleportCooldown");
            long currentTime = player.world.getWorldTime();
            player.getEntityData().setLong("TeleportCooldown", currentTime);
            if (currentTime > oldCooldown + 100) return;

            if (tpCooldownPotions == null) {
                tpCooldownPotions = new ArrayList<>();
                for (String potionString : tpCooldownPotionStrings) {
                    ResourceLocation location = new ResourceLocation(potionString);
                    if (ForgeRegistries.POTIONS.containsKey(location))
                        tpCooldownPotions.add(ForgeRegistries.POTIONS.getValue(location));
                    else
                        tpCooldownPotions.add(null);
                }
            }

            //Potion Sickness
            if (tpCooldownPotions.get(0) != null)
                player.addPotionEffect(new PotionEffect(tpCooldownPotions.get(0), 200, 1));

            //Surface Teleport
            if (tpCooldownPotions.get(1) != null)
                player.addPotionEffect(new PotionEffect(tpCooldownPotions.get(1), 5, 0));
        }
    }
}