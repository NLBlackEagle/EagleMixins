package eaglemixins.handlers;

import biomesoplenty.api.item.BOPItems;
import com.dhanantry.scapeandrunparasites.entity.ai.misc.EntityPStationaryArchitect;
import com.dhanantry.scapeandrunparasites.entity.ai.misc.EntityParasiteBase;
import com.dhanantry.scapeandrunparasites.entity.monster.deterrent.nexus.*;
import eaglemixins.config.ForgeConfigHandler;
import eaglemixins.util.Ref;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class SRParasitesHandler {
    //Parasites will be allowed to spawn via spawners, stay alive and will drop (reduced) loot in these biomes
    private static final List<String> overworldParasiteBiomes = Arrays.asList(
            "biomesoplenty:heath",
            "biomesoplenty:steppe",
            "biomesoplenty:wasteland",
            "openterraingenerator:overworld_abyssal_rift",
            "srparasites:biome_parasite",
            "openterraingenerator:overworld_lair_of_the_thing",
            "openterraingenerator:overworld_nuclear_ruins",
            "openterraingenerator:overworld_ruins_of_blight"
    );

    //Parasites with these names will always drop their loot without reduction, no matter the biome
    private static final List<String> parasiteNamesKeepDrops = Arrays.asList(
            "Sentient Horror",
            "Degrading Overseer",
            "Malformed Observer",
            "Shivaxi",
            "Corrupted Carrier",
            "Necrotic Blight"
    );

    private static ItemStack corruptedAshes = null;
    private static ItemStack getCorruptedAshes(){
        if(corruptedAshes == null){
            corruptedAshes = new ItemStack(BOPItems.ash,1);
            corruptedAshes.setTranslatableName("eaglemixins.tooltip.corruptedashes");
        }
        return corruptedAshes.copy();
    }

    private static boolean isBeckon(Entity entity){
        return entity instanceof EntityVenkrol ||
                entity instanceof EntityVenkrolSII ||
                entity instanceof EntityVenkrolSIII ||
                entity instanceof EntityVenkrolSIV ||
                entity instanceof EntityVenkrolSV;
    }

    // SRParasites in overworld Script Biome Whitelist, kill Beckons
    @SubscribeEvent
    public static void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        World world = entity.world;
        if (world.getTotalWorldTime() % 50 != 23) return;
        if (entity.dimension != 0 && entity.dimension != 3) return;

        if (!(entity instanceof EntityParasiteBase)) return;

        //Kill all Beckons and Dispatchers in Abyssal Rift
        if (Ref.entityIsInAbyssalRift(entity)) {
            if (entity instanceof EntityPStationaryArchitect)
                entity.setDead();
        //Otherwise kill all other beckons around one beckon
        } else if (isBeckon(entity))
            for (Entity entityNearby : entity.world.getEntitiesWithinAABB(EntityPStationaryArchitect.class, new AxisAlignedBB(entity.getPosition()).grow(32)))
                if (entityNearby != entity && isBeckon(entityNearby))
                    entityNearby.setDead();

        //Rest of this method is for overworld only
        if(entity.dimension == 3) return;

        //Slowly kill Parasites outside specific biomes
        ResourceLocation biomeReg = entity.world.getBiome(entity.getPosition()).getRegistryName();
        if (biomeReg != null && overworldParasiteBiomes.contains(biomeReg.toString())) return;

        float health = entity.getHealth();
        if (health > 1000)      entity.setHealth(health / 50);
        else if (health > 100)  entity.setHealth(health / 10);
        else                    entity.setHealth(health - 10);
    }

    // SRParasites in overworld Cancel Spawns if not in Whitelisted Biome and From spawner
    @SubscribeEvent
    public static void onCheckSpawn(LivingSpawnEvent.CheckSpawn event){
        if(!event.isSpawner()) return;
        if(event.getWorld().provider.getDimension()!=0) return;
        EntityLivingBase entity = event.getEntityLiving();
        ResourceLocation entityId = EntityList.getKey(entity);
        if(entityId == null) return;
        if(!(entity instanceof EntityParasiteBase)) return;
        ResourceLocation biomeReg = event.getWorld().getBiome(entity.getPosition()).getRegistryName();
        if(biomeReg != null && !overworldParasiteBiomes.contains(biomeReg.toString()))
            event.setResult(Event.Result.DENY);
    }

    // OW SRParasites cancel loot if not in whitelisted biome
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingDrops(LivingDropsEvent event){
        if(event.getDrops().isEmpty()) return;
        EntityLivingBase entity = event.getEntityLiving();
        if(entity.dimension != 0) return;
        ResourceLocation entityId = EntityList.getKey(entity);
        if(entityId == null) return;

        //Reduce enchanted book drops in Abyssal Rift (except from infernal mobs)
        if(Ref.entityIsInAbyssalRift(entity)) {
            List<EntityItem> itemsToRemove = new ArrayList<>();
            if (!entity.getEntityData().hasKey("InfernalMobsMod")) {
                for (EntityItem drop : event.getDrops()) {
                    if (drop.getItem().getItem().equals(Items.ENCHANTED_BOOK))
                        if (entity.getRNG().nextFloat() < 0.3)
                            itemsToRemove.add(drop);
                }
            }
            event.getDrops().removeAll(itemsToRemove);

            //Remove normal Shivaxi Boss drops in Abyssal Rift
            if(entityId.equals(Ref.playerBossReg)) {
                event.setCanceled(true);
                return;
            }
        }
        //Rest of this method only applies to parasites
        if(!(entity instanceof EntityParasiteBase)) return;

        //But not to ones that have special names
        if(entity.hasCustomName()) {
            String customName = entity.getName();
            for (String specialName : parasiteNamesKeepDrops)
                if (customName.contains(specialName))
                    return;
        }

        ResourceLocation biomeReg = entity.world.getBiome(entity.getPosition()).getRegistryName();

        if(biomeReg != null && overworldParasiteBiomes.contains(biomeReg.toString())){
            List<EntityItem> itemsToRemove = new ArrayList<>();
            List<EntityItem> itemsToAdd = new ArrayList<>();
            for (EntityItem drop : event.getDrops()) {
                ResourceLocation itemId = drop.getItem().getItem().getRegistryName();
                if(itemId == null) continue;
                if(itemId.getNamespace().equals(Ref.SRPMODID)) {
                    //Based of healthmultiplier 0.5 & damagemultiplier 0.25 averaged out on 0.625 the overall strength of ow parasites compared to LC parasites.
                    if (entity.getRNG().nextFloat() < 0.375) {
                        itemsToRemove.add(drop);
                        itemsToAdd.add(new EntityItem(entity.getEntityWorld(), entity.posX, entity.posY, entity.posZ, getCorruptedAshes()));
                    }
                }
            }
            event.getDrops().removeAll(itemsToRemove);
            event.getDrops().addAll(itemsToAdd);
        } else {
            event.getDrops().clear();
            event.setCanceled(true);
        }
    }

    private static final UUID atkUUID = UUID.fromString("b1880265-48be-4681-84b2-bf99bc3e16e1");
    private static final UUID hpUUID = UUID.fromString("0629ce1b-fdcf-4432-a9c0-1f51c588c615");
    private static final UUID armorUUID = UUID.fromString("34407975-a8dc-479c-8dcc-70cc914418dc");
    private static final AttributeModifier dmgMod = new AttributeModifier(atkUUID,"AbyssalDmg", ForgeConfigHandler.server.abyssalDmgModifier-1,1);
    private static final AttributeModifier hpMod = new AttributeModifier(hpUUID,"AbyssalHP", ForgeConfigHandler.server.abyssalHPModifier-1,1);
    private static final AttributeModifier armorMod = new AttributeModifier(armorUUID,"AbyssalArmor", ForgeConfigHandler.server.abyssalArmorModifier-1,1);

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
}
