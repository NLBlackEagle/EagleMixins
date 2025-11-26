package eaglemixins.compat;

import com.oblivioussp.spartanweaponry.item.ItemCrossbow;
import com.oblivioussp.spartanweaponry.item.ItemLongbow;
import com.oblivioussp.spartanweaponry.util.NBTHelper;
import eaglemixins.mixin.spartanweaponry.IItemCrossbow_InvokerMixin;
import net.minecraft.entity.EntityLiving;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * This is a very reduced copy thanks to not needing Throwing Weapon Handling
 */
public abstract class SpartanWeaponryUtil {

    public static boolean isHoldingSpartanBow(EntityLiving shooter){
        if(!shooter.getHeldItemMainhand().isEmpty()){
            Item item = shooter.getHeldItemMainhand().getItem();
            if(item instanceof ItemLongbow) return true;
            else if(item instanceof IItemCrossbow_InvokerMixin) return true;
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
}
