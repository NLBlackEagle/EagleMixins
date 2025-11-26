package eaglemixins.mixin.spartanweaponry;

import com.oblivioussp.spartanweaponry.init.EnchantmentRegistrySW;
import com.oblivioussp.spartanweaponry.init.ItemRegistrySW;
import com.oblivioussp.spartanweaponry.init.SoundRegistry;
import com.oblivioussp.spartanweaponry.item.ItemCrossbow;
import com.oblivioussp.spartanweaponry.item.ItemSW;
import com.oblivioussp.spartanweaponry.util.NBTHelper;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemCrossbow.class)
public abstract class ItemCrossbow_SkeletonUseMixin extends ItemSW {

    public ItemCrossbow_SkeletonUseMixin(String unlocName) {
        super(unlocName);
    }

    /**
     * Allows Skeletons to use without bricking
     */
    @Inject(
            method = "onItemUseFinish",
            at = @At("TAIL")
    )
    private void eagleMixins_spartanWeaponryItemCrossbow_onItemUseFinishMob(ItemStack stack, World worldIn, EntityLivingBase entityLiving, CallbackInfoReturnable<ItemStack> cir){
        if(entityLiving instanceof EntityLiving) {
            if(!NBTHelper.getBoolean(stack, ItemCrossbow.NBT_IS_LOADED)) {
                //could be any item, for skeletons we only care about the bolt count (3 if spreadshot). Will use the offhand bolt item when actually shooting
                ItemStack bolt = new ItemStack(ItemRegistrySW.bolt, EnchantmentHelper.getEnchantmentLevel(EnchantmentRegistrySW.CROSSBOW_SPREADSHOT, stack) > 0 ? 3 : 1);
                NBTHelper.setTagCompound(stack, ItemCrossbow.nbtAmmoStack, bolt.writeToNBT(new NBTTagCompound()));

                worldIn.playSound(null, entityLiving.posX, entityLiving.posY, entityLiving.posZ, SoundRegistry.CROSSBOW_LOAD, SoundCategory.NEUTRAL, 1.0F, 1.0F / (itemRand.nextFloat() * 0.4F + 1.2F) * 0.5F);
                NBTHelper.setBoolean(stack, ItemCrossbow.NBT_IS_LOADED, true);
            }
        }
    }
}
