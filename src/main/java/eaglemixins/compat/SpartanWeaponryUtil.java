package eaglemixins.compat;

import com.oblivioussp.spartanweaponry.entity.projectile.EntityBolt;
import com.oblivioussp.spartanweaponry.entity.projectile.EntityBoltTipped;
import com.oblivioussp.spartanweaponry.init.ItemRegistrySW;
import com.oblivioussp.spartanweaponry.init.SoundRegistry;
import com.oblivioussp.spartanweaponry.item.*;
import com.oblivioussp.spartanweaponry.util.NBTHelper;
import com.oblivioussp.spartanweaponry.util.Quaternion;
import eaglemixins.config.ForgeConfigHandler;
import eaglemixins.mixin.spartanweaponry.IItemCrossbow_Invoker;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.Vec3d;

/**
 * This is a very reduced copy thanks to not needing Throwing Weapon Handling
 */
public abstract class SpartanWeaponryUtil {
    public static boolean isSpartanTippedArrow(Item item){
        return item instanceof ItemArrowTipped;
    }

    public static EntityArrow createBolt(ItemStack offhandStack, float distanceFactor, EntityLivingBase shooter){
        //No offhand item -> use normal bolts. Same if modded arrows are disallowed
        if(!(offhandStack.getItem() instanceof ItemBolt) || !ForgeConfigHandler.mixintoggles.enabledModdedArrowsForAll)
            offhandStack = new ItemStack(ItemRegistrySW.bolt);

        EntityBolt entityBolt = ((ItemBolt) offhandStack.getItem()).createBolt(shooter.world, offhandStack, shooter);
        entityBolt.setEnchantmentEffectsFromEntity(shooter, (float) (entityBolt.getDamage() / 2. * (double) distanceFactor));

        if(offhandStack.getItem() == ItemRegistrySW.boltTipped && entityBolt instanceof EntityBoltTipped)
            ((EntityBoltTipped) entityBolt).setPotionEffect(offhandStack);

        return entityBolt;
    }

    public static boolean isTippedBolt(Item item){
        return item instanceof ItemBoltTipped;
    }

    public static boolean isSpartanCrossbow(Item item){
        return item instanceof ItemCrossbow;
    }

    public static boolean isSpartanLongbow(Item item){
        return item instanceof ItemLongbow;
    }

    public static boolean isHoldingSpartanRangedWeapon(EntityLivingBase shooter){
        if(!shooter.getHeldItemMainhand().isEmpty()){
            Item item = shooter.getHeldItemMainhand().getItem();
            if(item instanceof ItemLongbow) return true;
            else if(isSpartanCrossbow(item)) return true;
        }
        return false;
    }

    public static double getMaxVelocity(ItemStack itemStack){
        if(itemStack.getItem() instanceof ItemLongbow) return ((ItemLongbow) itemStack.getItem()).getMaxArrowSpeed();
        else if(itemStack.getItem() instanceof ItemCrossbow) return ((ItemCrossbow)itemStack.getItem()).getBoltSpeed();
        return 3.0D; // Vanilla Bow for player
    }

    public static int getAimAndLoadingTicks(ItemStack itemStack){
        if(itemStack.getItem() instanceof ItemCrossbow){
            boolean loadedBefore = NBTHelper.getBoolean(itemStack, ItemCrossbow.NBT_IS_LOADED);
            NBTHelper.setBoolean(itemStack, ItemCrossbow.NBT_IS_LOADED, false);
            int cooldown = itemStack.getItem().getMaxItemUseDuration(itemStack) + ((ItemCrossbow)itemStack.getItem()).getAimTicks(itemStack);
            NBTHelper.setBoolean(itemStack, ItemCrossbow.NBT_IS_LOADED, loadedBefore);

            return cooldown;
        }
        return getDrawSpeedTicks(itemStack); // Bows Load and Draw simultaneously
    }

    public static int getDrawSpeedTicks(ItemStack itemStack){
        if(itemStack.getItem() instanceof ItemLongbow) return ((ItemLongbow) itemStack.getItem()).getDrawTicks();
        else if(itemStack.getItem() instanceof ItemCrossbow){
            return NBTHelper.getBoolean(itemStack, ItemCrossbow.NBT_IS_LOADED) ?
                    ((ItemCrossbow)itemStack.getItem()).getAimTicks(itemStack) :
                    itemStack.getItem().getMaxItemUseDuration(itemStack);
        }
        return 20; // Vanilla Bow
    }

    public static Item getTippedBoltItem() {
        return ItemRegistrySW.boltTipped;
    }

    public static void addCrossbowNBT(ItemStack itemStack) {
        if(itemStack.getItem() instanceof ItemCrossbow) {
            NBTHelper.setBoolean(itemStack, ItemCrossbow.NBT_IS_LOADED, false);
            NBTHelper.setTagCompound(itemStack, ItemCrossbow.nbtAmmoStack, new NBTTagCompound());
        }
    }

    public static float getMaxLongbowArrowSpeed(Item item) {
        return ((ItemLongbow)item).getMaxArrowSpeed();
    }

    public static Vec3d getShootingVector(Item item, Vec3d lookVec, float rotationPitch, float rotationYaw, float projectileAngle) {
        Vec3d shooterUpVec = ((IItemCrossbow_Invoker) item).invokeCalculateEntityViewVector(
                rotationPitch - 90.0f,
                rotationYaw
        );
        Quaternion quat = new Quaternion(shooterUpVec, projectileAngle, true);
        return quat.transformVector(lookVec);
    }

    public static float getCrossbowBoltSpeed(Item item) {
        return ((ItemCrossbow) item).getBoltSpeed();
    }

    public static int getCrossbowShotCount(ItemStack stack) {
        int shots = 1;
        NBTTagCompound nbtLoadedBolt = NBTHelper.getTagCompound(stack, ItemCrossbow.nbtAmmoStack);
        if (nbtLoadedBolt.hasKey("Count") && nbtLoadedBolt.getInteger("Count") > 1) {
            shots = 3;
        }
        return shots;
    }

    public static SoundEvent getCrossbowFireSound() {
        return SoundRegistry.CROSSBOW_FIRE;
    }
}
