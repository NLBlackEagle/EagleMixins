package eaglemixins.handlers;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class BerryDebuffHandler {
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
            potion = Potion.getPotionFromResourceLocation("minecraft:nausea");
            if (potion != null)
                player.addPotionEffect(new PotionEffect(potion, duration, amplifier));
        }
        if (potionRoll < 7) {
            potion = Potion.getPotionFromResourceLocation("lycanitesmobs:aphagia");
            if (potion != null)
                player.addPotionEffect(new PotionEffect(potion, duration, amplifier));
        }
        if (potionRoll < 5) {
            potion = Potion.getPotionFromResourceLocation("simpledifficulty:parasites");
            if (potion != null)
                player.addPotionEffect(new PotionEffect(potion, duration, amplifier));
        }
        if (potionRoll < 3) {
            potion = Potion.getPotionFromResourceLocation("mod_lavacow:soiled");
            if (potion != null)
                player.addPotionEffect(new PotionEffect(potion, duration, amplifier));
        }
        if (potionRoll < 1) {
            potion = Potion.getPotionFromResourceLocation("potioncore:vulnerable");
            if (potion != null)
                player.addPotionEffect(new PotionEffect(potion, duration, amplifier));
        }
    }
}
