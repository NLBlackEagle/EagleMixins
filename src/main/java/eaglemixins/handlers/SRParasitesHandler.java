package eaglemixins.handlers;

import eaglemixins.EagleMixins;
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

        ResourceLocation entityId = EntityList.getKey(event.getEntityLiving());
        if (entityId == null) return;
        if (!entityId.getNamespace().equals("srparasites")) return;

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
        String biomeName = entity.world.getBiome(entity.getPosition()).getBiomeName();
        if (biomesParasitesStayAlive.contains(biomeName)) return;

        float health = entity.getHealth();

        if (biomeName.equals("Abyssal Rift")) {
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
        if(!entityId.getNamespace().equals("srparasites")) return;
        String biomeName = event.getWorld().getBiome(entity.getPosition()).getBiomeName();
        if(!biomesParasiteSpawnersAllowed.contains(biomeName))
            event.setResult(Event.Result.DENY);
    }

    private static final List<String> biomesParasiteDropsReduced = Arrays.asList(
            "Heath",
            "Steppe",
            "Wasteland",
            "Abyssal Rift",
            "Parasite Biome",
            "Ruins of Blight",
            "Nuclear Ruins",
            "Lair of the Thing"
    );

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
            Item item = Item.getByNameOrId("biomesoplenty:ash");
            if(item!=null) {
                corruptedAshes = new ItemStack(item,1);
                corruptedAshes.setTagInfo("Display",new NBTTagString("Corrupted Ashes"));
            } else
                corruptedAshes = ItemStack.EMPTY;
        }
        return corruptedAshes.copy();
    }

    // SRParasites in overworld Cancel loot if not in Whitelisted Biome
    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event){
        if(event.getDrops().isEmpty()) return;
        EntityLivingBase entity = event.getEntityLiving();
        if(entity.world.provider.getDimension() != 0) return;
        ResourceLocation entityId = EntityList.getKey(entity);
        if(entityId == null) return;
        String biomeName = entity.world.getBiome(entity.getPosition()).getBiomeName();

        //Keep drops inside Abyssal Rift except for book drops which are reduced (except for infernal mobs)
        if(biomeName.equals("Abyssal Rift")) {
            if (!entity.getEntityData().hasKey("InfernalMobsMod")) {
                List<EntityItem> drops = new ArrayList<>(event.getDrops());
                event.getDrops().clear();
                for (EntityItem drop : drops) {
                    if (drop.getItem().getItem().equals(Items.ENCHANTED_BOOK)) {
                        if (entity.getRNG().nextFloat() < 0.3)
                            event.getDrops().add(drop);
                    } else
                        event.getDrops().add(drop);
                }
            }
            if(entityId.toString().equals("playerbosses:player_boss"))
                event.setCanceled(true);
        }

        if(!entityId.getNamespace().equals("srparasites")) return;
        if(entity.hasCustomName()) {
            String customName = entity.getCustomNameTag();
            for (String specialName : parasiteNamesKeepDrops)
                if (customName.contains(specialName))
                    return;
        }

        if(biomesParasiteDropsReduced.contains(biomeName)){
            List<EntityItem> drops = new ArrayList<>(event.getDrops());
            event.getDrops().clear();
            for (EntityItem drop : drops) {
                ResourceLocation itemId = drop.getItem().getItem().getRegistryName();
                if(itemId == null) continue;
                if(itemId.getNamespace().equals("srparasites")) {
                    //Based of healthmultiplier 0.5 & damagemultiplier 0.25 averaged out on 0.625 the overall strength of parasites in the overworld compared to lost cities parasites.
                    if (entity.getRNG().nextFloat() < 0.625)
                        event.getDrops().add(drop);
                    else {
                        EagleMixins.LOGGER.info("corrupted ashes are air "+getCorruptedAshes().isEmpty());
                        event.getDrops().add(new EntityItem(entity.getEntityWorld(), entity.posX, entity.posY, entity.posZ, getCorruptedAshes()));
                    }

                } else
                    event.getDrops().add(drop);
            }
        } else {
            event.getDrops().clear();
            event.setCanceled(true);
        }
    }
}
