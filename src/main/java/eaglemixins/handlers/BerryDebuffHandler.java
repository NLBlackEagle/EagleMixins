package eaglemixins.handlers;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;

public class BerryDebuffHandler {
    private static ArrayList<Potion> berryPotions = null;
    private static final ArrayList<String> berryPotionStrings = new ArrayList<>(Arrays.asList("minecraft:nausea", "lycanitesmobs:aphagia", "simpledifficulty:parasites","mod_lavacow:soiled","potioncore:vulnerable"));

    @Nullable
    private static Potion getBerryPotion(int index) {
        if (berryPotions == null) {
            berryPotions = new ArrayList<>();
            for (String potionString : berryPotionStrings) {
                ResourceLocation location = new ResourceLocation(potionString);
                if (ForgeRegistries.POTIONS.containsKey(location))
                    berryPotions.add(ForgeRegistries.POTIONS.getValue(location));
                else
                    berryPotions.add(null);
            }
        }
        return berryPotions.get(index);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onItemuseFinish(LivingEntityUseItemEvent.Finish event) {
        if (event.getEntity().world.isRemote) return;
        EntityLivingBase entity = event.getEntityLiving();
        if (!(entity instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) entity;

        Item itemUsed = event.getItem().getItem();
        if (itemUsed.getRegistryName() == null) return;
        if (!itemUsed.getRegistryName().toString().equals("biomesoplenty:berries")) return;

        int duration = 60 + player.getRNG().nextInt(140);
        int amplifier = player.getRNG().nextInt(3);
        int potionRoll = player.getRNG().nextInt(100);

        Potion potion;
        if (potionRoll < 10) {
            potion = getBerryPotion(0);
            if (potion != null)
                player.addPotionEffect(new PotionEffect(potion, duration, amplifier));
        }
        if (potionRoll < 7) {
            potion = getBerryPotion(1);
            if (potion != null)
                player.addPotionEffect(new PotionEffect(potion, duration, amplifier));
        }
        if (potionRoll < 5) {
            potion = getBerryPotion(2);
            if (potion != null)
                player.addPotionEffect(new PotionEffect(potion, duration, amplifier));
        }
        if (potionRoll < 3) {
            potion = getBerryPotion(3);
            if (potion != null)
                player.addPotionEffect(new PotionEffect(potion, duration, amplifier));
        }
        if (potionRoll < 1) {
            potion = getBerryPotion(4);
            if (potion != null)
                player.addPotionEffect(new PotionEffect(potion, duration, amplifier));
        }
    }
}
