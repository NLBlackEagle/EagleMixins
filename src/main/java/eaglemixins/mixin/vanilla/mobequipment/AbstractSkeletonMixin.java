package eaglemixins.mixin.vanilla.mobequipment;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import eaglemixins.config.ForgeConfigHandler;
import eaglemixins.config.folders.MobEquipmentConfig;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.AbstractSkeleton;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntitySpectralArrow;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArrow;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(AbstractSkeleton.class)
public abstract class AbstractSkeletonMixin extends EntityLivingBase {
    public AbstractSkeletonMixin(World worldIn) {
        super(worldIn);
    }

    @ModifyArg(
            method = "setEquipmentBasedOnDifficulty",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;<init>(Lnet/minecraft/item/Item;)V")
    )
    private Item eaglemixins_vanillaAbstractSkeleton_setEquipmentBasedOnDifficulty(Item itemIn){
        return MobEquipmentConfig.getRandomItem(this.getRNG(), false);
    }

    @WrapMethod(
            method = "getArrow"
    )
    private EntityArrow eagleMixins_vanillaAbstractSkeleton_getArrowFromOffhandItem(float distanceFactor, Operation<EntityArrow> original){
        if(ForgeConfigHandler.mobequipment.enabledModdedArrowsForAll) {
            ItemStack itemstack = this.getItemStackFromSlot(EntityEquipmentSlot.OFFHAND);

            if (itemstack.getItem() == Items.SPECTRAL_ARROW) {
                EntitySpectralArrow entityspectralarrow = new EntitySpectralArrow(this.world, this);
                entityspectralarrow.setEnchantmentEffectsFromEntity(this, distanceFactor);
                return entityspectralarrow;
            } else if (itemstack.getItem() instanceof ItemArrow) {
                EntityArrow entityArrow = ((ItemArrow) itemstack.getItem()).createArrow(this.world, itemstack, this);
                entityArrow.setEnchantmentEffectsFromEntity(this, (float) (entityArrow.getDamage() * (distanceFactor / 2F)));
                return entityArrow;
            } else {
                EntityArrow entityarrow = original.call(distanceFactor);

                if (itemstack.getItem() == Items.TIPPED_ARROW && entityarrow instanceof EntityTippedArrow) {
                    ((EntityTippedArrow) entityarrow).setPotionEffect(itemstack);
                }

                return entityarrow;
            }
        }
        return original.call(distanceFactor);
    }
}
