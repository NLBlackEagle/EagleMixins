package eaglemixins.handlers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemChorusFruit;
import net.minecraft.item.ItemPotion;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionUtils;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.Arrays;

public class RandomTpCancelHandler {
    private static final ArrayList<String> biomeNames = new ArrayList<>(Arrays.asList("Parasite Biome","Abyssal Rift"));
    private static final ArrayList<String> tpPotions = new ArrayList<>(Arrays.asList("potioncore:teleport","potioncore:strong_teleport"));

    //Function to give chorus fruit & teleportation potions a different effect on use
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onItemUseFinish(LivingEntityUseItemEvent.Finish event) {
        EntityPlayer player = (EntityPlayer) event.getEntityLiving();
        if (player == null) return;
        String biomeName = player.world.getBiome(player.getPosition()).getBiomeName();
        if (!biomeNames.contains(biomeName)) return;

        Item usedItem = event.getItem().getItem();
        if (usedItem.getRegistryName() == null) return;
        if (!(usedItem instanceof ItemPotion) && !(usedItem instanceof ItemChorusFruit))
            return;

        if (usedItem instanceof ItemPotion) {
            boolean hasRandomTpPotion = false;
            for (PotionEffect effect : PotionUtils.getEffectsFromStack(event.getItem())) {
                if (effect.getPotion().getRegistryName() == null) continue;
                String potionId = effect.getPotion().getRegistryName().toString();
                if (tpPotions.contains(potionId)) {
                    hasRandomTpPotion = true;
                    break;
                }
            }
            if (!hasRandomTpPotion) return;
        }

        if (!player.getEntityData().hasKey("TeleportCooldown")) {
            player.getEntityData().setLong("TeleportCooldown", player.world.getTotalWorldTime());
        } else {
            long oldCooldown = player.getEntityData().getLong("TeleportCooldown");
            long currentTime = player.world.getWorldTime();
            player.getEntityData().setLong("TeleportCooldown", currentTime);
            if (currentTime > oldCooldown + 100) return;

            Potion potion = Potion.getPotionFromResourceLocation("potioncore:potion_sickness");
            if (potion != null) player.addPotionEffect(new PotionEffect(potion, 200, 1));
            
            potion = Potion.getPotionFromResourceLocation("potioncore:teleport_surface");
            if (potion != null) player.addPotionEffect(new PotionEffect(potion, 10, 0));
        }
    }
}