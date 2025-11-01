package eaglemixins.handlers;


import eaglemixins.config.ForgeConfigHandler;
import eaglemixins.util.Ref;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemChorusFruit;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.PotionEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.*;
import java.util.stream.Collectors;

public class RandomTpCancelHandler {

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public static void onLivingHurt(LivingHurtEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        if(entity.world.isRemote) return;
        if (!(entity instanceof EntityPlayer)) return;
        if (event.getSource() != DamageSource.IN_WALL) return;
        if (!tpMethods.contains("wallDmg")) return;
        if (!Ref.entityIsInAbyssalRift(entity) && !Ref.entityIsInAbyssalGate(entity)) return;

        applyTpCooldownDebuffs((EntityPlayer) entity);
    }

    //when throwing enderpearl
    @SubscribeEvent
    public static void onEnderPearlImpact(ProjectileImpactEvent.Throwable event) {
        if(!(event.getThrowable() instanceof EntityEnderPearl)) return;
        if(event.getThrowable().world.isRemote) return;
        EntityLivingBase entity = event.getThrowable().getThrower();
        if (!(entity instanceof EntityPlayer)) return;
        if (!tpMethods.contains("enderPearl")) return;
        if (!Ref.entityIsInAbyssalRift(entity) && !Ref.entityIsInAbyssalGate(entity)) return;

        applyTpCooldownDebuffs((EntityPlayer) entity);
    }

    //When eating chorus fruit
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onItemUseFinish(LivingEntityUseItemEvent.Finish event) {
        EntityLivingBase entity = event.getEntityLiving();
        if(entity.world.isRemote) return;
        if (!(entity instanceof EntityPlayer)) return;
        if (!(event.getItem().getItem() instanceof ItemChorusFruit)) return;
        if (!tpMethods.contains("chorusFruit")) return;
        if (!Ref.entityIsInAbyssalRift(entity) && !Ref.entityIsInAbyssalGate(entity)) return;

        applyTpCooldownDebuffs((EntityPlayer) entity);
    }

    //When applying a non-instant potion effect
    @SubscribeEvent
    public static void onPotionAdded(PotionEvent.PotionAddedEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        if(entity.world.isRemote) return;
        if (!(entity instanceof EntityPlayer)) return;
        if (!Ref.entityIsInAbyssalRift(entity) && !Ref.entityIsInAbyssalGate(entity)) return;

        if (isTpPotion(event.getPotionEffect().getPotion()))
            applyTpCooldownDebuffs((EntityPlayer) entity);
    }

    private static Set<Potion> tpPotions = null;
    public static Set<String> tpMethods = null;

    public static boolean isTpPotion(Potion potion) {
        if (tpPotions == null) {
            tpPotions = new HashSet<>();
            for (String potionString : ForgeConfigHandler.abyssal.randomTpPots) {
                if(!potionString.contains(":")) continue;
                ResourceLocation location = new ResourceLocation(potionString);
                if (ForgeRegistries.POTIONS.containsKey(location))
                    tpPotions.add(ForgeRegistries.POTIONS.getValue(location));
            }
        }
        return tpPotions.contains(potion);
    }

    private static List<Potion> tpCooldownPotions = null;
    private static final List<String> tpCooldownPotionStrings = new ArrayList<>(Arrays.asList(
            "potioncore:potion_sickness",
            "potioncore:teleport_surface"
    ));
    private static final String tpCooldownKey = "TeleportCooldown";

    public static void applyTpCooldownDebuffs(EntityPlayer player) {
        if (!player.getEntityData().hasKey(tpCooldownKey)) {
            // First time: start cooldown and exit
            player.getEntityData().setLong(tpCooldownKey, player.world.getTotalWorldTime());

        } else {
            final long now  = player.world.getTotalWorldTime();              // monotonic, never wraps
            final long last = player.getEntityData().getLong(tpCooldownKey);

            // Still on cooldown? bail
            if (now < last + 100L) return;

            // Cooldown elapsed: update timestamp and continue to apply effects
            player.getEntityData().setLong(tpCooldownKey, now);

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

            // Potion Sickness
            if (tpCooldownPotions.get(0) != null)
                player.addPotionEffect(new PotionEffect(tpCooldownPotions.get(0), 200, 1));

            // Surface Teleport
            if (tpCooldownPotions.get(1) != null)
                player.addPotionEffect(new PotionEffect(tpCooldownPotions.get(1), 1, 0));

        }
    }

    public static void refreshConfig(){
        tpPotions = null;
        tpMethods = Arrays.stream(ForgeConfigHandler.abyssal.randomTpPots)
                .filter(s -> !s.contains(":"))
                .collect(Collectors.toSet());
    }
}