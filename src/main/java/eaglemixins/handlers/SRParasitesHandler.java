package eaglemixins.handlers;

import biomesoplenty.api.item.BOPItems;
import com.dhanantry.scapeandrunparasites.entity.ai.misc.EntityPStationaryArchitect;
import com.dhanantry.scapeandrunparasites.entity.ai.misc.EntityParasiteBase;
import com.dhanantry.scapeandrunparasites.entity.monster.deterrent.nexus.*;
import eaglemixins.config.ForgeConfigHandler;
import eaglemixins.util.Ref;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
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
import java.util.List;

public class SRParasitesHandler {

    //Parasites will be allowed to spawn via spawners, stay alive and will drop (reduced) loot in these biomes
    //Handled in ForgeConfigHandler class
    public static boolean isBiomeAllowed(ResourceLocation biomeId) {
        String id = biomeId.toString();
        String modid = biomeId.getNamespace() + ":*";

        boolean isInList = ForgeConfigHandler.srparasites.getAllowedBiomeList().contains(modid) ||
                ForgeConfigHandler.srparasites.getAllowedBiomeList().stream().anyMatch(listedBiome -> listedBiome.equalsIgnoreCase(id));

        //true (allowed) if in list and whitelist, or not in list and blacklist
        //false (not allowed) if in list and blacklist, or not in list and whitelist
        return isInList == ForgeConfigHandler.srparasites.biomeListIsWhitelist;
    }

    private static ItemStack corruptedAshes = null;
    private static ItemStack getCorruptedAshes(){
        if(corruptedAshes == null){
            corruptedAshes = new ItemStack(BOPItems.ash,1);
            corruptedAshes.setTranslatableName("eaglemixins.tooltip.corruptedashes");
        }
        return corruptedAshes.copy();
    }

    private static boolean isBeckon(Entity entity){
        if(!(entity instanceof EntityPStationaryArchitect)) return false;
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
        if (ForgeConfigHandler.abyssal.killAbyssalNexus && Ref.entityIsInAbyssalRift(entity)) {
            if (entity instanceof EntityPStationaryArchitect)
                entity.setDead();
        //Otherwise kill all other beckons around one beckon
        } else if (ForgeConfigHandler.srparasites.killNearbyBeckon && isBeckon(entity))
            for (Entity entityNearby : entity.world.getEntitiesWithinAABB(EntityPStationaryArchitect.class, new AxisAlignedBB(entity.getPosition()).grow(32)))
                if (entityNearby != entity && isBeckon(entityNearby))
                    entityNearby.setDead();

        //Rest of this method is for overworld only
        if(entity.dimension == 3) return;

        //Only if enabled
        if(!ForgeConfigHandler.srparasites.killEscapedParasites) return;

        //Slowly kill Parasites outside specific biomes
        ResourceLocation biomeReg = entity.world.getBiome(entity.getPosition()).getRegistryName();
        if (biomeReg != null && SRParasitesHandler.isBiomeAllowed(biomeReg)) return;

        float health = entity.getHealth();
        if (health > 1000)      entity.setHealth(health / 50);
        else if (health > 100)  entity.setHealth(health / 10);
        else                    entity.setHealth(health - 10);
    }

    // SRParasites in overworld - cancel spawns if not in Whitelisted Biome and from spawner
    @SubscribeEvent
    public static void onCheckSpawn(LivingSpawnEvent.CheckSpawn event){
        if(!event.isSpawner()) return;
        if(event.getWorld().provider.getDimension()!=0) return;
        EntityLivingBase entity = event.getEntityLiving();
        if(!(entity instanceof EntityParasiteBase)) return;
        ResourceLocation biomeReg = event.getWorld().getBiome(entity.getPosition()).getRegistryName();
        if (biomeReg != null && !SRParasitesHandler.isBiomeAllowed(biomeReg))
            event.setResult(Event.Result.DENY);
    }

    // OW SRParasites cancel loot if not in whitelisted biome
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingDrops(LivingDropsEvent event){
        if(event.getDrops().isEmpty()) return;
        EntityLivingBase entity = event.getEntityLiving();

        //Only Overworld
        if(entity.dimension != 0) return;

        //Only for Parasites
        if(!(entity instanceof EntityParasiteBase)) return;

        //But not to ones that have special names
        if (entity.hasCustomName() &&
                ForgeConfigHandler.srparasites.getAllowedParasiteNamesLoot().stream().anyMatch(entity.getName()::contains))
            return;

        ResourceLocation biomeReg = entity.world.getBiome(entity.getPosition()).getRegistryName();

        if (biomeReg != null && !SRParasitesHandler.isBiomeAllowed(biomeReg)){
            List<EntityItem> itemsToRemove = new ArrayList<>();
            List<EntityItem> itemsToAdd = new ArrayList<>();
            for (EntityItem drop : event.getDrops()) {
                ResourceLocation itemId = drop.getItem().getItem().getRegistryName();
                if(itemId == null) continue;
                if(itemId.getNamespace().equals(Ref.SRPMODID)) {
                    //default 0.375 based of healthmultiplier 0.5 & damagemultiplier 0.25 averaged out on 0.625 the overall strength of ow parasites compared to LC parasites.
                    if (entity.getRNG().nextFloat() < ForgeConfigHandler.srparasites.chanceCorruptedAshes) {
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
