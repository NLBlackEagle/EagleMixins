package eaglemixins.handlers;

import eaglemixins.config.ForgeConfigHandler;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTippedArrow;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import java.util.Random;

public class RandomTippedArrowHandler {

    // Entities with tipped arrow in offhand apply potion effect on target hit
    // Use ForgeData: "ArrowEntity: anything" to include entities for changing arrows
    // Use ForgeData: "NoArrowSwitch: anything" to exclude entities from changing arrows

    //Randomly give some entities with bows offhanded tipped arrows
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        if (entity == null || entity.world.isRemote) return;

        ResourceLocation entityId = EntityList.getKey(entity);
        if (entityId == null || !ForgeConfigHandler.tippedarrows.getTippedArrowAllowedEntities().contains(entityId)) return;

        if (!(entity.getHeldItemMainhand().getItem() instanceof ItemBow)) return;

        NBTTagCompound tag = entity.getEntityData();
        if (tag.hasKey("ArrowCheck")) return;
        tag.setBoolean("ArrowCheck", true);

        //Entity already had a tipped arrow in offhand through other means, don't switch those
        if (entity.getHeldItemOffhand().getItem() instanceof ItemTippedArrow) {
            tag.setBoolean("NoArrowSwitch", true);
            return;
        }

        //Give entity tipped arrow
        if (entity.getRNG().nextFloat() <= ForgeConfigHandler.tippedarrows.tippedArrowReplacementChance) {
            tag.setBoolean("ArrowEntity", true);
            //Start with non-long arrow, chance to replace every hit
            entity.setItemStackToSlot(EntityEquipmentSlot.OFFHAND, ForgeConfigHandler.tippedarrows.getRandomArrowStack(entity.getRNG(), false));
        }
    }

    //Apply potion effects and switch tipped arrow
    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        EntityLivingBase victim = event.getEntityLiving();
        if (victim == null || victim.world.isRemote) return;

        if (event.getSource() == null || event.getSource().getTrueSource() == null) return;
        if (!(event.getSource().getTrueSource() instanceof EntityLivingBase)) return;
        if (!(event.getSource().getImmediateSource() instanceof EntityArrow)) return;

        EntityLivingBase attacker = (EntityLivingBase) event.getSource().getTrueSource();
        if(attacker instanceof EntityPlayer) return; //:skull:

        ItemStack mainhand = attacker.getHeldItemMainhand();
        if (!(mainhand.getItem() instanceof ItemBow)) return;
        ItemStack offhand = attacker.getHeldItemOffhand();
        if(!(offhand.getItem() instanceof ItemTippedArrow)) return;

        //Apply potion effects on attacked entity
        for (PotionEffect effect : PotionUtils.getEffectsFromStack(offhand)) {
            float durationMulti = 0.8F + 0.2F * attacker.getRNG().nextFloat();
            //TODO: eagle you can vary this multiplier if you really want a random duration. Rn its 80% to 100% of the original duration due to 0.8 + 0.2*[0 to 1]
            //TODO: i already added the vanilla 1/8 multi for duration so the usual 3mins turn to 22.5 secs
            // and the usual 8 mins turn to 1 minute
            // while instant potions stay instant lol
            //Copied from EntityTippedArrow, duration is downsized by 8 on tipped arrows, added multi
            victim.addPotionEffect(new PotionEffect(effect.getPotion(), (int) Math.max(effect.getDuration() / 8.F * durationMulti, 1F), effect.getAmplifier(), effect.getIsAmbient(), effect.doesShowParticles()));
        }

        //Swap held tipped arrow for some mobs
        if (attacker.getEntityData().hasKey("NoArrowSwitch")) return;
        if (!attacker.getEntityData().hasKey("ArrowEntity")) return;

        float rngRoll = attacker.getRNG().nextFloat();
        boolean doSwap = rngRoll < 0.2;
        boolean newArrowIsLong = rngRoll < 0.01;

        //Jester has higher chance to get long arrow (20%) and swaps every time (100%)
        //Other mobs have low chance for long arrow (1%) and only swap sometimes (20%)
        boolean isJester = attacker.hasCustomName() && attacker.getName().contains("Jester");
        if((isJester && doSwap) || (doSwap && newArrowIsLong))
            attacker.setItemStackToSlot(EntityEquipmentSlot.OFFHAND, ForgeConfigHandler.tippedarrows.getRandomArrowStack(attacker.getRNG(),true));
        else if(isJester || doSwap)
            attacker.setItemStackToSlot(EntityEquipmentSlot.OFFHAND, ForgeConfigHandler.tippedarrows.getRandomArrowStack(attacker.getRNG(),false));
    }
}
