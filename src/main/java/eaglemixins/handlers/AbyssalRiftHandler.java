package eaglemixins.handlers;

import com.dhanantry.scapeandrunparasites.entity.ai.misc.EntityParasiteBase;
import com.lothrazar.playerbosses.EntityPlayerBoss;
import eaglemixins.config.ForgeConfigHandler;
import eaglemixins.util.Ref;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Items;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static eaglemixins.handlers.util.AbyssalRiftSpawn.isAbyssalRiftSpawn;

public class AbyssalRiftHandler {
    private static final UUID atkUUID = UUID.fromString("b1880265-48be-4681-84b2-bf99bc3e16e1");
    private static final UUID hpUUID = UUID.fromString("0629ce1b-fdcf-4432-a9c0-1f51c588c615");
    private static final UUID armorUUID = UUID.fromString("34407975-a8dc-479c-8dcc-70cc914418dc");
    private static final AttributeModifier dmgMod = new AttributeModifier(atkUUID,"AbyssalDmg", ForgeConfigHandler.abyssal.abyssalDmgModifier-1,1);
    private static final AttributeModifier hpMod = new AttributeModifier(hpUUID,"AbyssalHP", ForgeConfigHandler.abyssal.abyssalHPModifier-1,1);
    private static final AttributeModifier armorMod = new AttributeModifier(armorUUID,"AbyssalArmor", ForgeConfigHandler.abyssal.abyssalArmorModifier-1,1);

    // Give paras in abyssal rift different stats
    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinWorldEvent event){
        if(!(event.getEntity() instanceof EntityParasiteBase)) return;
        EntityParasiteBase parasite = (EntityParasiteBase) event.getEntity();
        if(parasite.world.isRemote) return;
        if(Ref.entityIsInAbyssalRift(parasite)) {
            if(!parasite.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).hasModifier(dmgMod))
                parasite.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).applyModifier(dmgMod);
            if(!parasite.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).hasModifier(hpMod))
                parasite.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).applyModifier(hpMod);
            if(!parasite.getEntityAttribute(SharedMonsterAttributes.ARMOR).hasModifier(armorMod))
                parasite.getEntityAttribute(SharedMonsterAttributes.ARMOR).applyModifier(armorMod);
        }
    }

    // Cancel Shivaxi Playerboss drops and Enchanted Books with a chance
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingDrops(LivingDropsEvent event) {
        if (event.getDrops().isEmpty()) return;
        EntityLivingBase entity = event.getEntityLiving();
        if (entity.dimension != 0) return;

        if (entity instanceof EntityPlayerBoss && isAbyssalRiftSpawn(entity)) {
            event.setCanceled(true);
            return;
        }

        //Only in Abyssal Rift
        if (!Ref.entityIsInAbyssalRift(entity)) return;

        //Not for Infernal Mobs
        if (entity.getEntityData().hasKey("InfernalMobsMod")) return;

        //Reduce enchanted book drops
        List<EntityItem> itemsToRemove = new ArrayList<>();
        for (EntityItem drop : event.getDrops())
            if (drop.getItem().getItem().equals(Items.ENCHANTED_BOOK))
                if (entity.getRNG().nextFloat() < ForgeConfigHandler.abyssal.chanceEnchants)
                    itemsToRemove.add(drop);
        event.getDrops().removeAll(itemsToRemove);
    }
}
