package eaglemixins.handlers;

import eaglemixins.compat.ModLoadedUtil;
import eaglemixins.compat.SpartanWeaponryUtil;
import eaglemixins.config.ForgeConfigHandler;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemTippedArrow;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

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

        boolean hasBow = entity.getHeldItemMainhand().getItem() instanceof ItemBow;
        boolean hasCrossbow = !hasBow && ModLoadedUtil.spartanweaponry.isLoaded() && SpartanWeaponryUtil.isSpartanCrossbow(entity.getHeldItemMainhand().getItem());
        if (!hasBow && !hasCrossbow) return;

        NBTTagCompound tag = entity.getEntityData();
        if (tag.hasKey("ArrowCheck")) return;
        tag.setBoolean("ArrowCheck", true);

        //Entity already had a tipped arrow/bolt in offhand through other means, don't switch those
        if (isValidTippedItem(entity.getHeldItemOffhand().getItem(), hasCrossbow)) {
            tag.setBoolean("NoArrowSwitch", true);
            return;
        }

        //Give entity tipped arrow
        if (entity.getRNG().nextFloat() <= ForgeConfigHandler.tippedarrows.tippedArrowReplacementChance) {
            tag.setBoolean("ArrowEntity", true);
            //Start with non-long arrow, chance to replace every hit
            entity.setItemStackToSlot(EntityEquipmentSlot.OFFHAND, ForgeConfigHandler.tippedarrows.getRandomArrowStack(entity.getRNG(), false, hasCrossbow));
        }
    }

    //Switch tipped arrow on hit
    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        EntityLivingBase victim = event.getEntityLiving();
        if (victim == null || victim.world.isRemote) return;

        if (event.getSource() == null || event.getSource().getTrueSource() == null) return;
        if (!(event.getSource().getTrueSource() instanceof EntityLivingBase)) return;
        if (!(event.getSource().getImmediateSource() instanceof EntityArrow)) return;

        EntityLivingBase attacker = (EntityLivingBase) event.getSource().getTrueSource();
        if(attacker instanceof EntityPlayer) return; //:skull:

        if(attacker.getHeldItemMainhand().isEmpty()) return;
        if(attacker.getHeldItemOffhand().isEmpty()) return;
        boolean hasBow = attacker.getHeldItemMainhand().getItem() instanceof ItemBow; //includes spartan longbows
        boolean hasCrossbow = ModLoadedUtil.spartanweaponry.isLoaded() && SpartanWeaponryUtil.isSpartanCrossbow(attacker.getHeldItemMainhand().getItem());

        if(!((hasBow || hasCrossbow) && isValidTippedItem(attacker.getHeldItemOffhand().getItem(), hasCrossbow))) return;

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
            attacker.setItemStackToSlot(EntityEquipmentSlot.OFFHAND, ForgeConfigHandler.tippedarrows.getRandomArrowStack(attacker.getRNG(),true, hasCrossbow));
        else if(isJester || doSwap)
            attacker.setItemStackToSlot(EntityEquipmentSlot.OFFHAND, ForgeConfigHandler.tippedarrows.getRandomArrowStack(attacker.getRNG(),false, hasCrossbow));
    }

    private static boolean isValidTippedItem(Item arrow, boolean forCrossbow){
        boolean spartanLoaded = ModLoadedUtil.spartanweaponry.isLoaded();

        //Bows need tipped arrows
        boolean isTippedArrow = arrow instanceof ItemTippedArrow || spartanLoaded && SpartanWeaponryUtil.isSpartanTippedArrow(arrow);
        if(!forCrossbow && !isTippedArrow) return false;

        //Crossbows need tipped bolts
        boolean isTippedBolt = spartanLoaded && SpartanWeaponryUtil.isTippedBolt(arrow);
        if(forCrossbow && !isTippedBolt) return false;

        return true;
    }
}
