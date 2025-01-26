package eaglemixins.handlers;

import eaglemixins.EagleMixins;
import eaglemixins.util.Ref;
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
    //When eating chorus fruit
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onItemUseFinish(LivingEntityUseItemEvent.Finish event) {
        EntityLivingBase entity = event.getEntityLiving();
        if(entity.world.isRemote) return;
        if (!(entity instanceof EntityPlayer)) return;
        if (!(event.getItem().getItem() instanceof ItemChorusFruit)) return;
        if (!Ref.entityIsInAbyssalRift(entity)) return;

        applyTpCooldownDebuffs((EntityPlayer) entity);
    }

    //When applying a potion effect
    @SubscribeEvent
    public static void onPotionAdded(PotionEvent.PotionAddedEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        if(entity.world.isRemote) return;
        if (!(entity instanceof EntityPlayer)) return;
        if (!Ref.entityIsInAbyssalRift(entity)) return;

        if (isTpPotion(event.getPotionEffect().getPotion()))
            applyTpCooldownDebuffs((EntityPlayer) entity);
    }

    private static ArrayList<Potion> tpPotions = null;
    private static final ArrayList<String> tpPotionStrings = new ArrayList<>(Arrays.asList("potioncore:teleport", "potioncore:strong_teleport", "mujmajnkraftsbettersurvival:warp"));

    public static boolean isTpPotion(Potion potion) {
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

    public static void applyTpCooldownDebuffs(EntityPlayer player) {
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