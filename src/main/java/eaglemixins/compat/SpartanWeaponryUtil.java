package eaglemixins.compat;

import com.oblivioussp.spartanweaponry.init.ItemRegistrySW;
import com.oblivioussp.spartanweaponry.item.ItemArrowTipped;
import com.oblivioussp.spartanweaponry.item.ItemBoltTipped;
import com.oblivioussp.spartanweaponry.item.ItemCrossbow;
import com.oblivioussp.spartanweaponry.item.ItemLongbow;
import com.oblivioussp.spartanweaponry.util.NBTHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * This is a very reduced copy thanks to not needing Throwing Weapon Handling
 */
public abstract class SpartanWeaponryUtil {
    public static boolean isSpartanTippedArrow(Item item){
        return item instanceof ItemArrowTipped;
    }

    public static boolean isTippedBolt(Item item){
        return item instanceof ItemBoltTipped;
    }

    public static boolean isSpartanCrossbow(Item item){
        return item instanceof ItemCrossbow;
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
}
