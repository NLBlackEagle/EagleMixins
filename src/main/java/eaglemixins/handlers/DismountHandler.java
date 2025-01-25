package eaglemixins.handlers;

import eaglemixins.config.ForgeConfigHandler;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Arrays;
import java.util.List;

public class DismountHandler {
    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event){
        EntityLivingBase victim = event.getEntityLiving();
        if(!victim.isRiding()) return;
        if(event.getSource() == null) return;

        String damageType = event.getSource().getDamageType();
        boolean doDismount = damageType.equals("lightningBolt") || event.getAmount() > 6;

        if(event.getSource().getTrueSource() instanceof EntityLivingBase) {
            EntityLivingBase attacker = (EntityLivingBase) event.getSource().getTrueSource();
            if(attacker.hasCustomName())
                if(attacker.getCustomNameTag().contains("Dismounter") || attacker.getCustomNameTag().contains("Dismounting"))
                    doDismount = true;
        }
        if(damageType.equals("dragon_ice") || damageType.equals("dragon_fire") || damageType.equals("dragon_lightning")){
            for(ItemStack armor : victim.getArmorInventoryList()){
                ResourceLocation itemId = armor.getItem().getRegistryName();
                if(itemId == null) continue;
                //All armor pieces need to be iceandfire armor without "metal" in name (= dragon scale/tide guardian armor/etc)
                if(!(itemId.getNamespace().equals("iceandfire") && !itemId.getPath().contains("metal")))
                    doDismount = true;
            }
        }

        if(doDismount) {
            event.getEntityLiving().dismountRidingEntity();
            event.getEntityLiving().removePassengers();
        }
    }

    private static final List<String> biomeNames = Arrays.asList("Parasite Biome", "Abyssal Rift");

    @SubscribeEvent
    public static void onEntityMount(EntityMountEvent event){
        if(!event.isMounting()) return;
        if(event.getEntityMounting().world.isRemote) return;
        if(!(event.getEntityMounting() instanceof EntityPlayer)) return;
        if(!(event.getEntityBeingMounted() instanceof EntityLivingBase)) return;
        EntityLivingBase mount = (EntityLivingBase) event.getEntityBeingMounted();
        ResourceLocation mountId = EntityList.getKey(mount);
        if(mountId!=null)
            for(String allowedMount : ForgeConfigHandler.server.allowedAbyssalMounts)
                if(allowedMount.equals(mountId.toString()))
                    return;


        EntityPlayer player = (EntityPlayer) event.getEntityMounting();
        //TODO: all those biome name checks use a client side only method serverside. idk if thats a problem
        if (biomeNames.contains(player.world.getBiome(player.getPosition()).getBiomeName())){
            player.sendStatusMessage(new TextComponentTranslation("eaglemixins.messages.mountspooked", new Object[0]), true);
            event.setCanceled(true);
        }
    }

    private static final String dismountKey = "LastAbyssalDismountStrike";

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        EntityPlayer player = event.player;
        World world = player.world;
        if (world.isRemote) return;
        long worldTime = world.getTotalWorldTime();
        if (worldTime % 40 != 0) return;

        if (!player.isRiding()) return;
        if (!(player.getRidingEntity() instanceof EntityLivingBase)) return;

        //Deadblood always dismounts
        if(world.getBlockState(player.getPosition()).getBlock().getRegistryName().toString().equals("srparasites:deadblood")) {
            event.player.removePassengers();
            event.player.dismountRidingEntity();
            return;
        }

        //Otherwise handle Parasite Biome / Abyssal Rift dismount
        if (!biomeNames.contains(world.getBiome(player.getPosition()).getBiomeName())){
            player.getEntityData().setLong(dismountKey, 0);
            return;
        }

        ResourceLocation mountId = EntityList.getKey(player.getRidingEntity());
        if(mountId != null)
            for(String allowedMount : ForgeConfigHandler.server.allowedAbyssalMounts)
                if(allowedMount.equals(mountId.toString()))
                    return;

        if (!player.getEntityData().hasKey(dismountKey) || player.getEntityData().getLong(dismountKey) == 0) {
            player.getEntityData().setLong(dismountKey, worldTime);
            TextComponentTranslation msg = new TextComponentTranslation("eaglemixins.messages.ominousfeeling");
            player.sendStatusMessage(msg, true);
            player.sendStatusMessage(msg, false);
        } else if (worldTime > player.getEntityData().getLong(dismountKey) + 100) {
            player.getEntityData().setLong(dismountKey, 0);

            event.player.removePassengers();
            event.player.dismountRidingEntity();
            event.player.addPotionEffect(new PotionEffect(ConductivityHandler.getLightning()));
        }
    }
}