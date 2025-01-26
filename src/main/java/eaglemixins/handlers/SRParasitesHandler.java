package eaglemixins.handlers;

import biomesoplenty.api.item.BOPItems;
import eaglemixins.EagleMixins;
import eaglemixins.util.Ref;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SRParasitesHandler {
    private static final List<String> biomesParasitesStayAlive = Arrays.asList(
            "Heath",
            "Steppe",
            "Wasteland",
            "Frozen City Creek",
            "Desert City Creek",
            "Jungle City Creek",
            "Ruins of Blight",
            "Nuclear Ruins",
            "Lair of the Thing",
            "Parasite Biome"
    );

    // SRParasites in overworld Script Biome Whitelist, kill Beckons
    @SubscribeEvent
    public static void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        World world = entity.world;
        if (world.getTotalWorldTime() % 50 != 0) return;
        if (world.provider.getDimension() != 0 && world.provider.getDimension() != 3) return;

        ResourceLocation entityId = EntityList.getKey(entity);
        if (entityId == null) return;
        if (!entityId.getNamespace().equals(Ref.SRPMODID)) return;

        //Kill all other beckons around one beckon
        if (entityId.getPath().contains("beckon")) {
            for (Entity entity1 : entity.world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(entity.getPosition()).grow(32))) {
                if (entity1 == entity) continue;
                ResourceLocation entity1id = EntityList.getKey(entity1);
                if (entity1id == null) continue;
                if (entity1id.getPath().contains("beckon"))
                    entity1.setDead();
            }
        }

        if (world.provider.getDimension() == 3) return;

        //Slowly kill Parasites outside Abyssal Rift and the other named biomes
        //TODO: otg get biome instead of using this clientside function
        String biomeName = entity.world.getBiome(entity.getPosition()).getBiomeName();
        if (biomesParasitesStayAlive.contains(biomeName)) return;

        float health = entity.getHealth();

        if (Ref.entityIsInAbyssalRift(entity)) {
            if (entityId.getPath().contains("beckon") || entityId.getPath().contains("dispatcher"))
                entity.setDead();
        } else if (health > 1000)
            entity.setHealth(health / 50);
        else if (health > 100)
            entity.setHealth(health / 10);
        else
            entity.setHealth(health - 10);
    }

    private static final List<String> biomesParasiteSpawnersAllowed = Arrays.asList(
            "Heath",
            "Steppe",
            "Wasteland",
            "Abyssal Rift",
            "Parasite Biome",
            "Lair of the Thing",
            "Nuclear Ruins",
            "Ruins of Blight"
    );

    // SRParasites in overworld Cancel Spawns if not in Whitelisted Biome and From spawner
    @SubscribeEvent
    public static void onCheckSpawn(LivingSpawnEvent.CheckSpawn event){
        if(!event.isSpawner()) return;
        if(event.getWorld().provider.getDimension()!=0) return;
        EntityLivingBase entity = event.getEntityLiving();
        ResourceLocation entityId = EntityList.getKey(entity);
        if(entityId == null) return;
        if(!entityId.getNamespace().equals(Ref.SRPMODID)) return;
        String biomeName = event.getWorld().getBiome(entity.getPosition()).getBiomeName();
        //TODO: otg get biome instead of using this clientside function
        if(!biomesParasiteSpawnersAllowed.contains(biomeName))
            event.setResult(Event.Result.DENY);
    }

    private static final List<String> biomesParasiteDropsReduced = biomesParasiteSpawnersAllowed;
        /*Arrays.asList(
            "biomesoplenty:wasteland",
            "Steppe",
            "biomesoplenty:wasteland",
            "openterraingenerator:overworld_abyssal_rift",
            "srparasites:biome_parasite",
            "openterraingenerator:overworld_ruins_of_blight",
            "openterraingenerator:overworld_nuclear_ruins",
            "openterraingenerator:overworld_lair_of_the_thing"
    );*/

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
            corruptedAshes.setStackDisplayName("Corrupted Ashes");
        }
        return corruptedAshes.copy();
    }

    // OW SRParasites cancel loot if not in whitelisted biome
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingDrops(LivingDropsEvent event){
        if(event.getDrops().isEmpty()) return;
        EntityLivingBase entity = event.getEntityLiving();
        if(entity.dimension != 0) return;
        ResourceLocation entityId = EntityList.getKey(entity);
        if(entityId == null) return;
        //TODO: otg get biome instead of using this clientside function
        String biomeName = entity.world.getBiome(entity.getPosition()).getBiomeName();

        //Reduce enchanted book drops in Abyssal Rift (except from infernal mobs)
        // Translators note: don't ask me why
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
        if(!entityId.getNamespace().equals(Ref.SRPMODID)) return;

        //But not to ones that have special names
        if(entity.hasCustomName()) {
            String customName = entity.getName();
            for (String specialName : parasiteNamesKeepDrops)
                if (customName.contains(specialName))
                    return;
        }

        if(biomesParasiteDropsReduced.contains(biomeName)){
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
}
