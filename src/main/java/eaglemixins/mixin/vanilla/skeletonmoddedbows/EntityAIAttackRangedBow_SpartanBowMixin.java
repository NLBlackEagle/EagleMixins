package eaglemixins.mixin.vanilla.skeletonmoddedbows;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.oblivioussp.spartanweaponry.item.ItemCrossbow;
import com.oblivioussp.spartanweaponry.util.NBTHelper;
import eaglemixins.compat.SpartanWeaponryUtil;
import eaglemixins.config.ForgeConfigHandler;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.ai.EntityAIAttackRangedBow;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityAIAttackRangedBow.class)
public abstract class EntityAIAttackRangedBow_SpartanBowMixin<T extends EntityMob & IRangedAttackMob> {

    @Shadow @Final private T entity;

    @ModifyReturnValue(
            method = "isBowInMainhand",
            at = @At("RETURN")
    )
    private boolean spartanCombat_vanillaEntityAIAttackRangedBow_isBowInMainhandSpartan(boolean original){
        return original || SpartanWeaponryUtil.isHoldingSpartanBow(this.entity);
    }

    @ModifyExpressionValue(
            method = "updateTask",
            at = @At(value = "FIELD", target = "Lnet/minecraft/entity/ai/EntityAIAttackRangedBow;moveSpeedAmp:D")
    )
    private double spartanCombat_vanillaEntityAIAttackRangedBow_updateTaskMoveSpeedPenalty(double original){
        if(SpartanWeaponryUtil.isHoldingSpartanBow(this.entity) && ForgeConfigHandler.mobequipment.spartanSkeletons.enableMoveSpeedPenalty){
            return original / SpartanWeaponryUtil.getMaxVelocity(this.entity.getHeldItemMainhand());
        }
        return original;
    }

    @ModifyExpressionValue(
            method = "updateTask",
            at = @At(value = "FIELD", target = "Lnet/minecraft/entity/ai/EntityAIAttackRangedBow;maxAttackDistance:F")
    )
    private float spartanCombat_vanillaEntityAIAttackRangedBow_updateTaskStrafeDistanceBonus(float original){
        if(SpartanWeaponryUtil.isHoldingSpartanBow(this.entity) && ForgeConfigHandler.mobequipment.spartanSkeletons.enableStrafeDistanceBonus){
            return (float) (original * SpartanWeaponryUtil.getMaxVelocity(this.entity.getHeldItemMainhand()));
        }
        return original;
    }

    @ModifyConstant(
            method = "updateTask",
            constant = @Constant(intValue = 20, ordinal = 2)
    )
    private int spartanCombat_vanillaEntityAIAttackRangedBow_updateTaskAimDelay(int constant){
        if(SpartanWeaponryUtil.isHoldingSpartanBow(this.entity)){
            return SpartanWeaponryUtil.getDrawSpeedTicks(this.entity.getHeldItemMainhand());
        }
        return constant;
    }

    @ModifyExpressionValue(
            method = "updateTask",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemBow;getArrowVelocity(I)F")
    )
    private float spartanCombat_vanillaEntityAIAttackRangedBow_updateTaskGetMaxVelocity(float original){
        if(SpartanWeaponryUtil.isHoldingSpartanBow(this.entity)) {
            return (float) SpartanWeaponryUtil.getMaxVelocity(this.entity.getHeldItemMainhand());
        }
        return original;
    }

    @Inject(
            method = "updateTask",
            at = @At(value = "FIELD", target = "Lnet/minecraft/entity/ai/EntityAIAttackRangedBow;attackTime:I", ordinal = 0)
    )
    private void spartanCombat_vanillaEntityAIAttackRangedBow_updateTaskResetCrossbow(CallbackInfo ci){
        ItemStack itemStack = this.entity.getHeldItemMainhand();
        if(itemStack.getItem() instanceof ItemCrossbow) {
            NBTHelper.setBoolean(itemStack, ItemCrossbow.NBT_IS_LOADED, false);
            NBTHelper.setTagCompound(itemStack, ItemCrossbow.nbtAmmoStack, new NBTTagCompound());
        }
    }
}
